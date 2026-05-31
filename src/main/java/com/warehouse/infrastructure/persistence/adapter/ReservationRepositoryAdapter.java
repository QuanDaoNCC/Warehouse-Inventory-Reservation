package com.warehouse.infrastructure.persistence.adapter;

import com.warehouse.application.port.ReservationRepositoryPort;
import com.warehouse.domain.model.Reservation;
import com.warehouse.infrastructure.persistence.mapper.DomainMapper;
import com.warehouse.infrastructure.persistence.repository.SpringDataReservationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ReservationRepositoryAdapter implements ReservationRepositoryPort {

    private final SpringDataReservationRepository repository;

    public ReservationRepositoryAdapter(SpringDataReservationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return repository.findByIdWithItems(id).map(DomainMapper::toDomain);
    }

    @Override
    public Optional<Reservation> findByIdForUpdate(UUID id) {
        return repository.findByIdForUpdate(id).map(DomainMapper::toDomain);
    }

    @Override
    public Reservation save(Reservation reservation) {
        return DomainMapper.toDomain(repository.save(DomainMapper.toEntity(reservation)));
    }
}
