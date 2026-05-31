package com.warehouse.integration;

import com.warehouse.application.dto.CreateReservationRequest;
import com.warehouse.application.dto.ReservationItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ConcurrentReservationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("warehouse")
            .withUsername("warehouse")
            .withPassword("warehouse");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetInventory() {
        jdbcTemplate.update(
                "UPDATE inventory SET available_stock = 10, reserved_stock = 0, total_stock = 10 WHERE sku = 'A100'"
        );
    }

    @Test
    void concurrentReservations_onlyOneSucceedsWhenStockInsufficient() throws Exception {
        CreateReservationRequest request1 = new CreateReservationRequest(
                "ORD-CONCURRENT-1",
                List.of(new ReservationItemRequest("A100", 8))
        );
        CreateReservationRequest request2 = new CreateReservationRequest(
                "ORD-CONCURRENT-2",
                List.of(new ReservationItemRequest("A100", 8))
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        Future<ResponseEntity<Map>> future1 = executor.submit(() -> {
            startLatch.await();
            try {
                return restTemplate.exchange(
                        "/api/v1/reservations",
                        HttpMethod.POST,
                        new HttpEntity<>(request1),
                        Map.class
                );
            } finally {
                doneLatch.countDown();
            }
        });

        Future<ResponseEntity<Map>> future2 = executor.submit(() -> {
            startLatch.await();
            try {
                return restTemplate.exchange(
                        "/api/v1/reservations",
                        HttpMethod.POST,
                        new HttpEntity<>(request2),
                        Map.class
                );
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        ResponseEntity<Map> response1 = future1.get();
        ResponseEntity<Map> response2 = future2.get();

        long successCount = List.of(response1, response2).stream()
                .filter(response -> response.getStatusCode() == HttpStatus.CREATED)
                .count();
        long conflictCount = List.of(response1, response2).stream()
                .filter(response -> response.getStatusCode() == HttpStatus.CONFLICT)
                .count();

        assertThat(successCount).isEqualTo(1);
        assertThat(conflictCount).isEqualTo(1);

        Integer availableStock = jdbcTemplate.queryForObject(
                "SELECT available_stock FROM inventory WHERE sku = 'A100'",
                Integer.class
        );
        Integer reservedStock = jdbcTemplate.queryForObject(
                "SELECT reserved_stock FROM inventory WHERE sku = 'A100'",
                Integer.class
        );

        assertThat(availableStock).isEqualTo(2);
        assertThat(reservedStock).isEqualTo(8);
    }
}
