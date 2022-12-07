package ru.practicum.shareit.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTests {

    @Mock
    BookingRepository mockBookingRepo;
    @Mock
    ItemRepository mockItemRepo;
    @Mock
    UserRepository mockUserRepo;
    BookingService service;
    BookingMapper mapper = new BookingMapperImpl();

    LocalDateTime moment = LocalDateTime.now();

    @BeforeEach
    void createService() {
        service = new BookingService(mockBookingRepo, mapper, mockUserRepo, mockItemRepo);
    }

    @Test
    void testCreateBooking() {
        User itemOwner = makeItemOwner(1);
        User requester = makeBooker(2);
        Item item = new Item(1, "testItem", "testing_1", Boolean.TRUE, itemOwner, null);
        Mockito.when(mockItemRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(item));
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(requester));
        Mockito.when(mockBookingRepo.save(Mockito.any(Booking.class))).thenAnswer(i -> i.getArguments()[0]);
        BookingRequest request = new BookingRequest(1, moment, moment.plusDays(1));
        BookingDto result = service.create(request, 2);

        assertThat(result.getStart(), equalTo(request.getStart()));
        assertThat(result.getEnd(), equalTo(request.getEnd()));
        assertThat(result.getItemId(), equalTo(request.getItemId()));
        assertThat(result.getBooker().getName(), equalTo(requester.getName()));
        assertThat(result.getItem().getName(), equalTo(item.getName()));
        assertThat(result.getBookerId(), equalTo(2));
        assertThat(result.getStatus(), equalTo(BookingStatus.WAITING.name()));
    }

    @Test
    void testCreateBookingItemNotFound() {
        Mockito.when(mockItemRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        BookingRequest request = new BookingRequest(1, moment, moment.plusDays(1));
        assertThrows(NotFoundException.class, () -> service.create(request, 3));
    }

    @Test
    void testCreateBookingItemIsNotAvailable() {
        Item item = new Item(1, "testItem", "testing_1", Boolean.FALSE,
                makeItemOwner(5), null);
        Mockito.when(mockItemRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(item));
        BookingRequest request = new BookingRequest(1, moment, moment.plusDays(1));
        assertThrows(BookingCreateException.class, () -> service.create(request, 4));
    }

    @Test
    void testCreateBookingSelfItem() {
        Item item = new Item(1, "testItem", "testing_1",
                Boolean.TRUE, makeItemOwner(49), null);
        Mockito.when(mockItemRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(item));
        BookingRequest request = new BookingRequest(1, moment, moment.plusDays(1));
        assertThrows(NotFoundException.class, () -> service.create(request, 49));
    }

    @Test
    void testCreateBookingRequesterNotFound() {
        Item item = new Item(1, "testItem", "testing_1",
                Boolean.TRUE, makeItemOwner(49), null);
        Mockito.when(mockItemRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(item));
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        BookingRequest request = new BookingRequest(1, moment, moment.plusDays(1));
        assertThrows(NotFoundException.class, () -> service.create(request, 5));
    }

    @Test
    void testChangeStatusByOwnerApproved() {
        Item item = new Item(1, "testItem", "testing_1",
                Boolean.TRUE, makeItemOwner(49), null);
        Booking booking = new Booking(1, moment, moment.plusDays(1), item, makeBooker(1), BookingStatus.WAITING);
        Mockito.when(mockBookingRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(booking));
        BookingDto result = service.changeStatusByOwner(1, Boolean.TRUE, 49);
        assertThat(result.getStatus(), equalTo(BookingStatus.APPROVED.name()));
    }

    @Test
    void testChangeStatusByOwnerRejected() {
        Item item = new Item(1, "testItem", "testing_1",
                Boolean.TRUE, makeItemOwner(49), null);
        Booking booking = new Booking(1, moment, moment.plusDays(1), item, makeBooker(1), BookingStatus.WAITING);
        Mockito.when(mockBookingRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(booking));
        BookingDto result = service.changeStatusByOwner(1, Boolean.FALSE, 49);
        assertThat(result.getStatus(), equalTo(BookingStatus.REJECTED.name()));
    }

    @Test
    void testChangeStatusBookingNotFound() {
        Mockito.when(mockBookingRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.changeStatusByOwner(1, Boolean.TRUE, 1));
    }

    @Test
    void testChangeStatusUserNotOwner() {
        Item item = new Item(1, "testItem", "testing_1",
                Boolean.TRUE, makeItemOwner(49), null);
        Booking booking = new Booking(1, moment, moment.plusDays(1), item, makeBooker(1), BookingStatus.WAITING);
        Mockito.when(mockBookingRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class, () -> service.changeStatusByOwner(1, Boolean.TRUE, 48));
    }

    @Test
    void testChangeStatusAlreadyApproved() {
        Item item = new Item(1, "testItem", "testing_1",
                Boolean.TRUE, makeItemOwner(49), null);
        Booking booking = new Booking(1, moment, moment.plusDays(1), item, makeBooker(1), BookingStatus.APPROVED);
        Mockito.when(mockBookingRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(booking));
        assertThrows(BookingStatusChangeException.class, () -> service
                .changeStatusByOwner(1, Boolean.TRUE, 49));
    }

    @Test
    void testGetById() {
        Item item = new Item(1, "testItem", "testing_1",
                Boolean.TRUE, makeItemOwner(49), null);
        Booking booking = new Booking(1, moment, moment.plusDays(1), item, makeBooker(1), BookingStatus.WAITING);
        Mockito.when(mockBookingRepo.findById(1)).thenReturn(Optional.of(booking));
        BookingDto result1 = service.getById(1, 1); //Проверка запроса букером
        assertThat(result1.getItem().getName(), equalTo(item.getName()));
        assertThat(result1.getStatus(), equalTo(booking.getStatus().name()));
        BookingDto result2 = service.getById(1, 49); //Проверка запроса владельцем вещи
        assertEquals(result1, result2);
    }

    @Test
    void testGetByIdNotFound() {
        Mockito.when(mockBookingRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(1, 1));
    }

    @Test
    void testGetByIdNotBookerOrOwner() {
        Item item = new Item(1, "testItem", "testing_1",
                Boolean.TRUE, makeItemOwner(49), null);
        Booking booking = new Booking(1, moment, moment.plusDays(1), item, makeBooker(1), BookingStatus.WAITING);
        Mockito.when(mockBookingRepo.findById(1)).thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class, () -> service.getById(1, 35));
    }

    @Test
    void testGetAllByUsers() {
        User user = makeBooker(1);
        List<BookingDto> plannedList = mapper.toDtoList(makeBookingsList());
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(user));
        Mockito.when(mockBookingRepo.findByBookerId(Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        Mockito.when(mockBookingRepo.findByBookerIdAndStartIsBeforeAndEndIsAfter(Mockito.anyInt(),
                        Mockito.any(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        Mockito.when(mockBookingRepo.findByBookerIdAndEndIsBefore(Mockito.anyInt(),
                        Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        Mockito.when(mockBookingRepo.findByBookerIdAndStartIsAfter(Mockito.anyInt(),
                        Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        Mockito.when(mockBookingRepo.findByBookerIdAndStatusEquals(Mockito.anyInt(),
                        Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        assertThat(service.getAllByUser("ALL", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUser("CURRENT", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUser("PAST", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUser("FUTURE", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUser("WAITING", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUser("REJECTED", 1, 0, 20), equalTo(plannedList));
    }

    @Test
    void testGetAllByUsersBookerNotFound() {
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getAllByUser("ALL", 1, 0, 20));
    }

    @Test
    void testGetAllByUserStateIllegal() {
        User user = makeBooker(1);
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(user));
        assertThrows(BookingBadRequest.class, () -> service.getAllByUser("BRUH", 1, 0, 20));
    }

    @Test
    void testGetAllByUserOwner() {
        User user = makeBooker(1);
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(user));
        List<BookingDto> plannedList = mapper.toDtoList(makeBookingsList());
        List<Item> itemsList = List.of(new Item(), new Item());
        int i = 0;
        for (Item item : itemsList) {
            item.setId(i++);
        }
        Mockito.when(mockItemRepo.findByOwnerId(Mockito.any())).thenReturn(itemsList);
        Mockito.when(mockBookingRepo.findByItemIdIn(Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        Mockito.when(mockBookingRepo.findByItemIdInAndStartIsBeforeAndEndIsAfter(Mockito.any(),
                        Mockito.any(), Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        Mockito.when(mockBookingRepo.findByItemIdInAndEndIsBefore(Mockito.any(),
                        Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        Mockito.when(mockBookingRepo.findByItemIdInAndStartIsAfter(Mockito.any(),
                        Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        Mockito.when(mockBookingRepo.findByItemIdInAndStatusEquals(Mockito.any(),
                        Mockito.any(), Mockito.any(Pageable.class)))
                .thenReturn(makeBookingsList());
        assertThat(service.getAllByUserOwner("ALL", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUserOwner("CURRENT", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUserOwner("PAST", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUserOwner("FUTURE", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUserOwner("WAITING", 1, 0, 20), equalTo(plannedList));
        assertThat(service.getAllByUserOwner("REJECTED", 1, 0, 20), equalTo(plannedList));
    }

    @Test
    void testGetAllByUsersOwnerNotFound() {
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getAllByUserOwner("ALL", 1, 0, 20));
    }

    @Test
    void testBookingMapper() {
        Booking booking = makeBooking(1);
        BookingDto bookingDto = mapper.toDto(booking);
        Booking returnBooking = mapper.toEntity(bookingDto);
        assertThat(booking, equalTo(returnBooking));
        List<Booking> bookings = makeBookingsList();
        List<BookingDtoShort> shortBookings = mapper.toDtoShortList(bookings);
        assertThat(shortBookings.size(), equalTo(bookings.size()));
        assertNull(mapper.toDtoShortList(null));
        Item item = new Item(1, "testItem", "testing_1",
                Boolean.TRUE, makeItemOwner(49), null);
        BookingDto.MiniItem miniItem = mapper.toMiniItem(item);
        assertThat(miniItem.getId(), equalTo(item.getId()));
        assertThat(miniItem.getName(), equalTo(item.getName()));
        User booker = makeBooker(1);
        BookingDto.MiniBooker miniBooker = mapper.toMiniBooker(booker);
        assertThat(miniBooker.getId(), equalTo(booker.getId()));
        assertThat(miniBooker.getName(), equalTo(booker.getName()));
        bookingDto.setBooker(miniBooker);
        bookingDto.setItem(miniItem);
        Booking newBooking = mapper.toEntity(bookingDto);
        assertThat(newBooking.getItem().getName(), equalTo(miniItem.getName()));
        assertThat(newBooking.getBooker().getName(), equalTo(miniBooker.getName()));
    }

    private User makeItemOwner(Integer id) {
        return new User(id, "testOwner", "test@ya.ru");
    }

    private User makeBooker(Integer id) {
        return new User(id, "testRequester", "request@mail.ru");
    }

    private List<Booking> makeBookingsList() {
        List<Booking> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(new Booking(i, moment.plusSeconds(i), moment.plusHours(i),
                    null, null, BookingStatus.WAITING));
        }
        return list;
    }

    private Booking makeBooking(Integer id) {
        return new Booking(id, moment.plusSeconds(id), moment.plusHours(id),
                null, null, BookingStatus.WAITING);
    }
}
