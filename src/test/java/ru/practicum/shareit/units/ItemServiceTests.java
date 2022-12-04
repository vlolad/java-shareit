package ru.practicum.shareit.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemBadRequestException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.CommentMapperImpl;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Comment;
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
public class ItemServiceTests {

    @Mock
    ItemRepository mockItemRepo;
    @Mock
    UserRepository mockUserRepo;
    ItemMapper itemMapper = new ItemMapperImpl();
    @Mock
    BookingRepository mockBookingRepo;
    BookingMapper bookingMapper = new BookingMapperImpl();
    CommentMapper commentMapper = new CommentMapperImpl();
    @Mock
    CommentRepository mockCommentRepo;
    ItemService service;
    LocalDateTime moment = LocalDateTime.now();

    @BeforeEach
    void makeService() {
        service = new ItemService(mockItemRepo, mockUserRepo, itemMapper, mockBookingRepo,
                bookingMapper, commentMapper, mockCommentRepo);
    }

    @Test
    void testItemMapper() {
        CreateItemRequest request = makeCreateItemRequest(1);
        Item item = itemMapper.toEntity(request);
        assertThat(request.getId(), equalTo(item.getId()));
        assertThat(request.getName(), equalTo(item.getName()));
        assertThat(request.getDescription(), equalTo(item.getDescription()));
        assertThat(request.getAvailable(), equalTo(item.getAvailable()));
        assertNull(item.getRequestId());

        Item item2 = makeItem(2);
        ItemDto itemDto2 = itemMapper.toDto(item2);
        assertThat(item2.getId(), equalTo(itemDto2.getId()));
        assertThat(item2.getName(), equalTo(itemDto2.getName()));
        assertThat(item2.getAvailable(), equalTo(itemDto2.getAvailable()));
        assertNull(itemDto2.getLastBooking());
        assertNull(itemDto2.getNextBooking());
        assertNull(itemDto2.getComments());
        assertThat(item2, equalTo(itemMapper.toEntity(itemDto2)));
    }

    @Test
    void testCreateItem() {
        User owner = makeItemOwner(1);
        Mockito.when(mockUserRepo.findById(1)).thenReturn(Optional.of(owner));
        Mockito.when(mockItemRepo.save(Mockito.any(Item.class))).thenAnswer(i -> i.getArguments()[0]);
        CreateItemRequest request = makeCreateItemRequest(1);
        ItemDto result = service.create(request, 1);
        assertThat(result.getId(), equalTo(1));
        assertThat(result.getName(), equalTo(request.getName()));
    }

    @Test
    void testCreateItemOwnerNotFound() {
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(makeCreateItemRequest(1), 1));
    }

    @Test
    void testPatchItem() {
        Item item = makeItem(1);
        item.setOwner(makeItemOwner(1));
        Mockito.when(mockItemRepo.findById(1)).thenReturn(Optional.of(item));
        CreateItemRequest newItem = makeCreateItemRequest(2);
        newItem.setId(1);
        newItem.setAvailable(Boolean.FALSE);
        ItemDto result = service.patchItem(newItem, 1);
        assertThat(result.getName(), equalTo(newItem.getName()));
        assertThat(result.getDescription(), equalTo(newItem.getDescription()));
        assertThat(result.getAvailable(), equalTo(newItem.getAvailable()));
    }

    @Test
    void testPatchItemNotFound() {
        Mockito.when(mockItemRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.patchItem(makeCreateItemRequest(1), 1));
    }

    @Test
    void testPatchItemForbidden() {
        Item item = makeItem(1);
        item.setOwner(makeItemOwner(1));
        Mockito.when(mockItemRepo.findById(1)).thenReturn(Optional.of(item));
        assertThrows(ItemBadRequestException.class, () -> service.patchItem(makeCreateItemRequest(1), 47));
    }

    @Test
    void testGetItem() {
        Item item = makeItem(1);
        item.setOwner(makeItemOwner(1));
        Mockito.when(mockItemRepo.findById(1)).thenReturn(Optional.of(item));
        Mockito.when(mockCommentRepo.findAllByItemIdOrderByCreatedDesc(1)).thenReturn(makeCommentsList());
        ItemDto result = service.getItem(1, 2);
        assertThat(item.getName(), equalTo(result.getName()));
        assertThat(result.getComments().size(), equalTo(3));
        assertNull(result.getNextBooking());
        assertNull(result.getLastBooking());
    }

    @Test
    void testGetItemByOwner() {
        Item item = makeItem(1);
        item.setOwner(makeItemOwner(1));
        Mockito.when(mockItemRepo.findById(1)).thenReturn(Optional.of(item));
        Mockito.when(mockCommentRepo.findAllByItemIdOrderByCreatedDesc(1)).thenReturn(makeCommentsList());
        Mockito.when(mockBookingRepo.findByItemIdAndEndIsBefore(Mockito.anyInt(), Mockito.any()))
                .thenReturn(makeBooking(1));
        Mockito.when(mockBookingRepo.findByItemIdAndStartIsAfter(Mockito.anyInt(), Mockito.any()))
                .thenReturn(makeBooking(2));
        ItemDto result = service.getItem(1, item.getOwner().getId());
        assertThat(item.getName(), equalTo(result.getName()));
        assertThat(result.getComments().size(), equalTo(3));
        assertThat(result.getLastBooking().getStart(), equalTo(moment.minusDays(1)));
        assertThat(result.getNextBooking().getEnd(), equalTo(moment.plusHours(2 * 2)));
    }

    @Test
    void testGetItemNotFound() {
        Mockito.when(mockItemRepo.findById(1)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getItem(1, 1));
    }

    @Test
    void testGetAllByOwner() {
        User owner = makeItemOwner(1);
        Mockito.when(mockUserRepo.findById(1)).thenReturn(Optional.of(owner));
        Mockito.when(mockItemRepo.findByOwnerId(Mockito.anyInt(), Mockito.any(Pageable.class))).thenReturn(makeItemsList());
        Mockito.when(mockBookingRepo.findByItemIdInAndStatusIs(Mockito.anyList(), Mockito.any(), Mockito.any(Sort.class)))
                .thenReturn(makeBookingsList());
        Mockito.when(mockCommentRepo.findAllByItemIdInOrderByCreatedDesc(Mockito.anyList())).thenReturn(makeCommentsList());
        List<ItemDto> result = service.getAllByOwner(1, 0, 20);
        assertThat(result.get(2).getComments().size(), equalTo(1));
    }

    @Test
    void testGetAllByOwnerNotFound() {
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getAllByOwner(1, 0, 20));
    }

    @Test
    void testSearch() {
        Mockito.when(mockItemRepo.search(Mockito.anyString(), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(makeItemsList()));
        assertFalse(service.search("text", 0, 20).isEmpty());
    }

    @Test
    void testCreateComment() {
        User user = makeItemOwner(1);
        Mockito.when(mockUserRepo.findById(1)).thenReturn(Optional.of(user));
        List<Booking> bookings = makeBookingsList();
        bookings.get(0).setStatus(BookingStatus.APPROVED);
        Mockito.when(mockBookingRepo.findAllByBookerIdAndItemIdAndEndIsBefore(Mockito.anyInt(),
                Mockito.anyInt(), Mockito.any())).thenReturn(bookings);
        Item item = makeItem(1);
        item.setOwner(makeItemOwner(5));
        Mockito.when(mockItemRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(item));
        Mockito.when(mockCommentRepo.save(Mockito.any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);
        CommentDto result = service.createComment(commentMapper.toDto(makeComment(1)), 1);
        assertThat(result.getAuthorName(), equalTo(user.getName()));
        assertThat(result.getItemId(), equalTo(item.getId()));
        assertEquals(commentMapper.toEntity(result), makeComment(1));
    }

    private User makeItemOwner(Integer id) {
        return new User(id, "testOwner" + id, "test" + id + "@ya.ru");
    }

    private CreateItemRequest makeCreateItemRequest(Integer id) {
        return new CreateItemRequest(id, "item" + id, "testing_create", Boolean.TRUE, null);
    }

    private Item makeItem(Integer id) {
        return new Item(id, "item" + id, "test_item", Boolean.TRUE, null, null);
    }

    private List<Item> makeItemsList() {
        List<Item> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(makeItem(i));
        }
        return list;
    }

    private Comment makeComment(Integer id) {
        Item item = new Item();
        item.setId(id);
        return new Comment(id, "text" + id, item, null, LocalDateTime.now());
    }

    private List<Comment> makeCommentsList() {
        List<Comment> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(makeComment(i));
        }
        return list;
    }

    private Booking makeBooking(Integer id) {
        Item item = new Item();
        item.setId(id);
        return new Booking(id, moment.minusDays(id), moment.plusHours(id * 2),
                item, null, BookingStatus.WAITING);
    }

    private List<Booking> makeBookingsList() {
        List<Booking> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(makeBooking(i));
        }
        return list;
    }
}
