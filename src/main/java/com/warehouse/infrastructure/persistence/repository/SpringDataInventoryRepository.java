package com.warehouse.infrastructure.persistence.repository;

import com.warehouse.infrastructure.persistence.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpringDataInventoryRepository extends JpaRepository<InventoryEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM InventoryEntity i WHERE i.sku IN :skus ORDER BY i.sku ASC")
    List<InventoryEntity> findBySkusForUpdate(@Param("skus") Collection<String> skus);

    Optional<InventoryEntity> findBySku(String sku);
}
