package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.exception.BookingBadRequest;
import ru.practicum.shareit.booking.exception.BookingCreateException;
import ru.practicum.shareit.booking.exception.BookingStatusChangeException;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BookingService(BookingRepository bookingRepository, BookingMapper bookingMapper,
                          UserRepository userRepository, ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public BookingDto create(BookingRequest request, Integer requesterId) {
        Optional<Item> item = itemRepository.findById(request.getItemId());
        if (item.isEmpty()) throw new NotFoundException("Item not found.");
        if (item.get().getAvailable().equals(Boolean.FALSE))
            throw new BookingCreateException("This item not available.");
        if (item.get().getOwner().getId().equals(requesterId))
            throw new NotFoundException("Booking your item? Why?");
        Booking booking = bookingMapper.toEntityFromRequest(request);
        booking.setItem(item.get());

        Optional<User> requester = userRepository.findById(requesterId);
        if (requester.isEmpty()) throw new NotFoundException("Requester not found by ID.");
        booking.setBooker(requester.get());
        booking.setStatus(BookingStatus.WAITING);
        log.info("Saving new booking: {}", booking);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    public BookingDto changeStatusByOwner(Integer bookingId, Boolean approved, Integer ownerId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) throw new NotFoundException("Booking not found.");

        User owner = booking.get().getItem().getOwner();
        if (!owner.getId().equals(ownerId)) throw new NotFoundException("This is not user's booking.");
        if (booking.get().getStatus().equals(BookingStatus.APPROVED))
            throw new BookingStatusChangeException("Booking already approved");

        if (approved.equals(Boolean.TRUE)) {
            log.info("Approve booking id:{}", bookingId);
            booking.get().setStatus(BookingStatus.APPROVED);
        } else {
            log.info("Reject booking id:{}", bookingId);
            booking.get().setStatus(BookingStatus.REJECTED);
        }
        return bookingMapper.toDto(bookingRepository.save(booking.get()));
    }

    public BookingDto getById(Integer bookingId, Integer userId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) throw new NotFoundException("Booking not found.");

        if (!booking.get().getBooker().getId().equals(userId)
                && !booking.get().getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("This is not user's booking or item.");
        }
        log.info("Found booking.");
        return bookingMapper.toDto(booking.get());
    }

    public List<BookingDto> getAllByUser(String state, Integer userId) {
        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("Requester not found");
        StateMode stateMode;
        try {
            stateMode = StateMode.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BookingBadRequest("Unknown state: " + state);
        }
        Sort sort = Sort.by("start").descending();
        switch (stateMode) {
            case ALL:
                return bookingMapper.toDtoList(bookingRepository.findByBookerId(userId, sort));
            case CURRENT:
                LocalDateTime moment = LocalDateTime.now();
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId,
                        moment, moment, sort));
            case PAST:
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndEndIsBefore(
                        userId, LocalDateTime.now(), sort));
            case FUTURE:
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndStartIsAfter(
                        userId, LocalDateTime.now(), sort));
            case WAITING:
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndStatusEquals(userId,
                        BookingStatus.WAITING, sort));
            case REJECTED:
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndStatusEquals(userId,
                        BookingStatus.REJECTED, sort));
            default:
                throw new BookingBadRequest("There is no state");
        }
    }

    public List<BookingDto> getAllByUserOwner(String state, Integer userId) {
        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("Requester not found");
        StateMode stateMode;
        try {
            stateMode = StateMode.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new BookingBadRequest("Unknown state: " + state);
        }
        List<Item> userItems = itemRepository.findByOwnerId(userId);
        List<Integer> userItemsIds = userItems.stream().map(Item::getId).collect(Collectors.toList());
        Sort sort = Sort.by("start").descending();
        switch (stateMode) {
            case ALL:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdIn(userItemsIds, sort));
            case CURRENT:
                LocalDateTime moment = LocalDateTime.now();
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndStartIsBeforeAndEndIsAfter(
                        userItemsIds, moment, moment, sort));
            case PAST:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndEndIsBefore(
                        userItemsIds, LocalDateTime.now(), sort));
            case FUTURE:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndStartIsAfter(
                        userItemsIds, LocalDateTime.now(), sort));
            case WAITING:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndStatusEquals(
                        userItemsIds, BookingStatus.WAITING, sort));
            case REJECTED:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndStatusEquals(
                        userItemsIds, BookingStatus.REJECTED, sort));
            default:
                throw new BookingBadRequest("There is no state");
        }
    }
}