package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public BookingDto create(BookingRequest request, Integer requesterId) {
        Optional<Item> item = itemRepository.findById(request.getItemId());
        if (item.isEmpty()) throw new NotFoundException("Item not found.");
        if (item.get().getOwner().getId().equals(requesterId))
            throw new NotFoundException("Booking your item? Why?");
        if (item.get().getAvailable().equals(Boolean.FALSE))
            throw new BookingCreateException("This item not available.");
        Booking booking = bookingMapper.toEntityFromRequest(request);
        booking.setItem(item.get());

        Optional<User> requester = userRepository.findById(requesterId);
        if (requester.isEmpty()) throw new NotFoundException("Requester not found by ID.");
        booking.setBooker(requester.get());
        booking.setStatus(BookingStatus.WAITING);
        log.info("Saving new booking: {}", booking);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto changeStatusByOwner(Integer bookingId, Boolean approved, Integer ownerId) {
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) throw new NotFoundException("Booking not found.");

        User owner = booking.get().getItem().getOwner();
        if (!owner.getId().equals(ownerId)) throw new NotFoundException("This is not user's item.");
        if (booking.get().getStatus().equals(BookingStatus.APPROVED))
            throw new BookingStatusChangeException("Booking already approved");

        if (approved.equals(Boolean.TRUE)) {
            log.info("Approve booking id:{}", bookingId);
            booking.get().setStatus(BookingStatus.APPROVED);
        } else {
            log.info("Reject booking id:{}", bookingId);
            booking.get().setStatus(BookingStatus.REJECTED);
        }
        return bookingMapper.toDto(booking.get());
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public List<BookingDto> getAllByUser(String state, Integer userId, Integer from, Integer size) {
        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("Requester not found");
        StateMode stateMode = StateMode.parseState(state);
        Pageable page = PageRequest.of(from / size, size, Sort.by("start").descending());
        switch (stateMode) {
            case ALL:
                return bookingMapper.toDtoList(bookingRepository.findByBookerId(userId, page));
            case CURRENT:
                LocalDateTime moment = LocalDateTime.now();
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId,
                        moment, moment, page));
            case PAST:
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndEndIsBefore(
                        userId, LocalDateTime.now(), page));
            case FUTURE:
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndStartIsAfter(
                        userId, LocalDateTime.now(), page));
            case WAITING:
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndStatusEquals(userId,
                        BookingStatus.WAITING, page));
            case REJECTED:
                return bookingMapper.toDtoList(bookingRepository.findByBookerIdAndStatusEquals(userId,
                        BookingStatus.REJECTED, page));
            default:
                throw new BookingBadRequest("There is no state");
        }
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getAllByUserOwner(String state, Integer userId, Integer from, Integer size) {
        if (userRepository.findById(userId).isEmpty())
            throw new NotFoundException("Requester not found");
        StateMode stateMode = StateMode.parseState(state);
        List<Item> userItems = itemRepository.findByOwnerId(userId);
        List<Integer> userItemsIds = userItems.stream().map(Item::getId).collect(Collectors.toList());
        Pageable page = PageRequest.of(from / size, size, Sort.by("start").descending());
        switch (stateMode) {
            case ALL:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdIn(userItemsIds, page));
            case CURRENT:
                LocalDateTime moment = LocalDateTime.now();
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndStartIsBeforeAndEndIsAfter(
                        userItemsIds, moment, moment, page));
            case PAST:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndEndIsBefore(
                        userItemsIds, LocalDateTime.now(), page));
            case FUTURE:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndStartIsAfter(
                        userItemsIds, LocalDateTime.now(), page));
            case WAITING:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndStatusEquals(
                        userItemsIds, BookingStatus.WAITING, page));
            case REJECTED:
                return bookingMapper.toDtoList(bookingRepository.findByItemIdInAndStatusEquals(
                        userItemsIds, BookingStatus.REJECTED, page));
            default:
                throw new BookingBadRequest("There is no state");
        }
    }
}
