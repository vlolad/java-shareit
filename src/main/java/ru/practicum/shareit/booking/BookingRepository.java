package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByBookerId(Integer bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Integer bookerId,
                                                              LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Integer bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Integer bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStatusEquals(Integer bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItemIdIn(List<Integer> items, Sort sort);
    List<Booking> findByItemIdInAndStatusIs(List<Integer> items, BookingStatus status, Sort sort);

    List<Booking> findByItemIdInAndStartIsBeforeAndEndIsAfter(List<Integer> items,
                                                              LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findByItemIdInAndEndIsBefore(List<Integer> items, LocalDateTime end, Sort sort);

    List<Booking> findByItemIdInAndStartIsAfter(List<Integer> items, LocalDateTime start, Sort sort);

    List<Booking> findByItemIdInAndStatusEquals(List<Integer> items, BookingStatus status, Sort sort);

    Booking findByItemIdAndEndIsBefore(Integer itemId, LocalDateTime end);

    Booking findByItemIdAndStartIsAfter(Integer itemId, LocalDateTime start);

    List<Booking> findAllByBookerIdAndItemIdAndEndIsBefore(Integer bookerId, Integer itemId, LocalDateTime end);
}
