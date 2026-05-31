package com.warehouse.infrastructure.persistence.repository;

import com.warehouse.infrastructure.persistence.entity.ReservationEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataReservationRepository extends JpaRepository<ReservationEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM ReservationEntity r WHERE r.id = :id")
    Optional<ReservationEntity> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT r FROM ReservationEntity r LEFT JOIN FETCH r.items WHERE r.id = :id")
    Optional<ReservationEntity> findByIdWithItems(@Param("id") UUID id);
}
