package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByBookerId(Integer bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Integer bookerId,
                                                              LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndIsBefore(Integer bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartIsAfter(Integer bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatusEquals(Integer bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemIdIn(List<Integer> items, Pageable pageable);

    List<Booking> findByItemIdInAndStatusIs(List<Integer> items, BookingStatus status, Pageable pageable);

    List<Booking> findByItemIdInAndStatusIs(List<Integer> items, BookingStatus status, Sort sort);

    List<Booking> findByItemIdInAndStartIsBeforeAndEndIsAfter(List<Integer> items,
                                                              LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemIdInAndEndIsBefore(List<Integer> items, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemIdInAndStartIsAfter(List<Integer> items, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemIdInAndStatusEquals(List<Integer> items, BookingStatus status, Pageable pageable);

    Booking findTopByItemIdAndEndIsBefore(Integer itemId, LocalDateTime end);

    Booking findTopByItemIdAndStartIsAfter(Integer itemId, LocalDateTime start);

    List<Booking> findAllByBookerIdAndItemIdAndEndIsBefore(Integer bookerId, Integer itemId, LocalDateTime end);
}
