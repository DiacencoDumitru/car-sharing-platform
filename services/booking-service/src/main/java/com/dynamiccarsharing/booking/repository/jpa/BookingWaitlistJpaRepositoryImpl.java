package com.dynamiccarsharing.booking.repository.jpa;

import com.dynamiccarsharing.booking.model.BookingWaitlistEntry;
import com.dynamiccarsharing.booking.model.BookingWaitlistStatus;
import com.dynamiccarsharing.booking.repository.BookingWaitlistRepository;
import com.dynamiccarsharing.util.filter.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("jpa")
@RequiredArgsConstructor
public class BookingWaitlistJpaRepositoryImpl implements BookingWaitlistRepository {

    private final InternalBookingWaitlistJpaRepository repository;

    @Override
    public Optional<BookingWaitlistEntry> findActiveById(Long id) {
        return repository.findActiveById(id);
    }

    @Override
    public Optional<BookingWaitlistEntry> findActiveDuplicate(Long renterId, Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        return repository.findActiveDuplicate(renterId, carId, startTime, endTime);
    }

    @Override
    public List<BookingWaitlistEntry> findOverlappingByCarAndStatus(Long carId, LocalDateTime startTime, LocalDateTime endTime, BookingWaitlistStatus status) {
        return repository.findOverlappingByCarAndStatus(carId, startTime, endTime, status);
    }

    @Override
    public BookingWaitlistEntry save(BookingWaitlistEntry entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<BookingWaitlistEntry> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<BookingWaitlistEntry> findAll() {
        return repository.findAll();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<BookingWaitlistEntry> findByFilter(Filter<BookingWaitlistEntry> filter) throws SQLException {
        throw new UnsupportedOperationException("Waitlist filtering is not implemented");
    }
}
