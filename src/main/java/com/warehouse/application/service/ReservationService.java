package com.warehouse.application.service;

import com.warehouse.application.dto.CreateReservationRequest;
import com.warehouse.application.dto.InventoryResponse;
import com.warehouse.application.dto.ReservationResponse;
import com.warehouse.application.port.InventoryRepositoryPort;
import com.warehouse.application.port.ReservationRepositoryPort;
import com.warehouse.domain.exception.InsufficientStockException;
import com.warehouse.domain.exception.InventoryNotFoundException;
import com.warehouse.domain.exception.ReservationNotFoundException;
import com.warehouse.domain.factory.ReservationFactory;
import com.warehouse.domain.model.Inventory;
import com.warehouse.domain.model.Reservation;
import com.warehouse.domain.model.ReservationItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepositoryPort reservationRepository;
    private final InventoryRepositoryPort inventoryRepository;
    private final ReservationFactory reservationFactory;

    public ReservationService(
            ReservationRepositoryPort reservationRepository,
            InventoryRepositoryPort inventoryRepository,
            ReservationFactory reservationFactory) {
        this.reservationRepository = reservationRepository;
        this.inventoryRepository = inventoryRepository;
        this.reservationFactory = reservationFactory;
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        Map<String, Integer> quantitiesBySku = aggregateQuantities(request);

        List<String> sortedSkus = quantitiesBySku.keySet().stream()
                .sorted()
                .toList();

        List<Inventory> lockedInventory = inventoryRepository.findBySkusForUpdate(sortedSkus);
        validateAllSkusFound(sortedSkus, lockedInventory);
        validateSufficientStock(quantitiesBySku, lockedInventory);

        for (Inventory inventory : lockedInventory) {
            inventory.reserve(quantitiesBySku.get(inventory.getSku()));
            inventoryRepository.save(inventory);
        }

        List<ReservationFactory.ItemRequest> itemRequests = quantitiesBySku.entrySet().stream()
                .map(entry -> new ReservationFactory.ItemRequest(entry.getKey(), entry.getValue()))
                .toList();

        Reservation reservation = reservationFactory.create(request.orderId(), itemRequests);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse confirmReservation(UUID id) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.confirm();
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancelReservation(UUID id) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        reservation.cancel();

        List<String> sortedSkus = reservation.getItems().stream()
                .map(ReservationItem::getSku)
                .sorted()
                .distinct()
                .toList();

        List<Inventory> lockedInventory = inventoryRepository.findBySkusForUpdate(sortedSkus);
        Map<String, Inventory> inventoryBySku = lockedInventory.stream()
                .collect(Collectors.toMap(Inventory::getSku, inventory -> inventory));

        for (ReservationItem item : reservation.getItems()) {
            Inventory inventory = inventoryBySku.get(item.getSku());
            if (inventory == null) {
                throw new InventoryNotFoundException(item.getSku());
            }
            inventory.release(item.getQuantity());
            inventoryRepository.save(inventory);
        }

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventory(String sku) {
        Inventory inventory = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException(sku));
        return InventoryResponse.from(inventory);
    }

    private Map<String, Integer> aggregateQuantities(CreateReservationRequest request) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (var item : request.items()) {
            quantities.merge(item.sku(), item.quantity(), Integer::sum);
        }
        return quantities;
    }

    private void validateAllSkusFound(List<String> requestedSkus, List<Inventory> found) {
        if (found.size() != requestedSkus.size()) {
            List<String> foundSkus = found.stream().map(Inventory::getSku).toList();
            String missing = requestedSkus.stream()
                    .filter(sku -> !foundSkus.contains(sku))
                    .findFirst()
                    .orElseThrow();
            throw new InventoryNotFoundException(missing);
        }
    }

    private void validateSufficientStock(Map<String, Integer> quantitiesBySku, List<Inventory> inventoryList) {
        Map<String, Inventory> inventoryBySku = inventoryList.stream()
                .collect(Collectors.toMap(Inventory::getSku, inventory -> inventory));

        List<String> insufficientSkus = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : quantitiesBySku.entrySet()) {
            Inventory inventory = inventoryBySku.get(entry.getKey());
            if (inventory.getAvailableStock() < entry.getValue()) {
                insufficientSkus.add(entry.getKey());
            }
        }

        if (!insufficientSkus.isEmpty()) {
            String sku = insufficientSkus.stream().min(Comparator.naturalOrder()).orElseThrow();
            throw new InsufficientStockException(
                    sku,
                    quantitiesBySku.get(sku),
                    inventoryBySku.get(sku).getAvailableStock()
            );
        }
    }
}
