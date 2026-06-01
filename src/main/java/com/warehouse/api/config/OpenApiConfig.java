package com.warehouse.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Warehouse Inventory Reservation API",
                version = "v1",
                description = "Concurrent-safe inventory reservation endpoints"
        )
)
public class OpenApiConfig {
}
