package com.warehouse.application.port;

import com.warehouse.domain.model.Inventory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InventoryRepositoryPort {

    Optional<Inventory> findBySku(String sku);

    List<Inventory> findBySkusForUpdate(Collection<String> skus);

    Inventory save(Inventory inventory);
}
