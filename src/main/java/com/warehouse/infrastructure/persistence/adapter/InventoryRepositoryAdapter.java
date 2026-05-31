package com.warehouse.infrastructure.persistence.adapter;

import com.warehouse.application.port.InventoryRepositoryPort;
import com.warehouse.domain.model.Inventory;
import com.warehouse.infrastructure.persistence.mapper.DomainMapper;
import com.warehouse.infrastructure.persistence.repository.SpringDataInventoryRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class InventoryRepositoryAdapter implements InventoryRepositoryPort {

    private final SpringDataInventoryRepository repository;

    public InventoryRepositoryAdapter(SpringDataInventoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Inventory> findBySku(String sku) {
        return repository.findBySku(sku).map(DomainMapper::toDomain);
    }

    @Override
    public List<Inventory> findBySkusForUpdate(Collection<String> skus) {
        return repository.findBySkusForUpdate(skus).stream()
                .map(DomainMapper::toDomain)
                .toList();
    }

    @Override
    public Inventory save(Inventory inventory) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(inventory)));
    }
}
