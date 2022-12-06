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
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShort;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTests {

    @Mock
    ItemRequestRepository mockRepo;
    @Mock
    UserRepository mockUserRepo;
    ItemRequestMapper mapper = new ItemRequestMapperImpl();
    @Mock
    ItemRepository mockItemRepo;
    ItemMapper itemMapper = new ItemMapperImpl();
    ItemRequestService service;
    LocalDateTime moment = LocalDateTime.now();

    @BeforeEach
    void makeService() {
        service = new ItemRequestService(mockRepo, mockUserRepo, mockItemRepo, mapper, itemMapper);
    }

    @Test
    void testItemRequestMapper() {
        User requester = makeUser(1);
        ItemRequest request = makeRequest(1);
        request.setRequester(requester);
        ItemRequestDto requestDto = mapper.toDto(request);
        assertThat(request.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(request.getCreated(), equalTo(requestDto.getCreated()));
        assertNotNull(requestDto.getItems());
        assertTrue(requestDto.getItems().isEmpty());
        ItemRequestShort shortRequest = makeShortRequest();
        ItemRequest shortEntity = mapper.toEntity(shortRequest);
        assertThat(shortEntity.getDescription(), equalTo(shortRequest.getDescription()));
    }

    @Test
    void testCreateRequest() {
        User user = makeUser(1);
        when(mockUserRepo.findById(1)).thenReturn(Optional.of(user));
        when(mockRepo.save(Mockito.any(ItemRequest.class))).thenAnswer(i -> i.getArguments()[0]);
        ItemRequestShort request = makeShortRequest();
        ItemRequestDto result = service.create(request, 1);
        assertThat(result.getDescription(), equalTo(request.getDescription()));
        assertTrue(result.getCreated().isAfter(moment));
    }

    @Test
    void testCreateRequestNotFound() {
        when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(makeShortRequest(), 1));
    }

    @Test
    void testGetAllByUser() {
        when(mockUserRepo.findById(1)).thenReturn(Optional.of(new User()));
        when(mockRepo.findAllByRequesterId(Mockito.anyInt(), Mockito.any(Sort.class)))
                .thenReturn(makeRequestsList());
        when(mockItemRepo.findAllByRequestIdIn(Mockito.anyList())).thenReturn(makeItemsList());
        List<ItemRequestDto> result = service.getByUser(1);
        assertThat(result.size(), equalTo(3));
        assertFalse(result.get(0).getItems().isEmpty());
    }

    @Test
    void testGetAllByUserNotFound() {
        when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getByUser(1));
    }

    @Test
    void testGetAll() {
        when(mockUserRepo.findById(1)).thenReturn(Optional.of(new User()));
        when(mockRepo.findAllByRequesterIdNot(Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(makeRequestsList()));
        when(mockItemRepo.findAllByRequestIdIn(Mockito.anyList())).thenReturn(makeItemsList());
        List<ItemRequestDto> result = service.getAll(1, 0, 20);
        assertThat(result.size(), equalTo(3));
        assertFalse(result.get(0).getItems().isEmpty());
    }

    @Test
    void testGetAllNoItems() {
        when(mockUserRepo.findById(1)).thenReturn(Optional.of(new User()));
        when(mockRepo.findAllByRequesterIdNot(Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<>(makeRequestsList()));
        when(mockItemRepo.findAllByRequestIdIn(Mockito.anyList())).thenReturn(List.of());
        List<ItemRequestDto> result = service.getAll(1, 0, 20);
        assertThat(result.size(), equalTo(3));
        assertTrue(result.get(0).getItems().isEmpty());

        when(mockItemRepo.findAllByRequestIdIn(Mockito.anyList())).thenReturn(null);
        List<ItemRequestDto> result2 = service.getAll(1, 0, 20);
        assertThat(result2.size(), equalTo(3));
        assertTrue(result2.get(0).getItems().isEmpty());
    }

    @Test
    void testGetAllNotFound() {
        when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getAll(1, 0, 20));
    }

    @Test
    void testGetById() {
        when(mockUserRepo.findById(1)).thenReturn(Optional.of(new User()));
        when(mockRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(makeRequest(1)));
        when(mockItemRepo.findAllByRequestId(Mockito.anyInt())).thenReturn(makeItemsList());
        ItemRequestDto result = service.getById(1, 1);
        assertThat(result.getDescription(), equalTo(makeRequest(1).getDescription()));
        assertThat(result.getItems().size(), equalTo(3));
    }

    @Test
    void testGetByIdNoItems() {
        when(mockUserRepo.findById(1)).thenReturn(Optional.of(new User()));
        when(mockRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(makeRequest(1)));
        when(mockItemRepo.findAllByRequestId(Mockito.anyInt())).thenReturn(List.of());
        ItemRequestDto result = service.getById(1, 1);
        assertThat(result.getDescription(), equalTo(makeRequest(1).getDescription()));
        assertTrue(result.getItems().isEmpty());

        when(mockItemRepo.findAllByRequestId(Mockito.anyInt())).thenReturn(null);
        ItemRequestDto result2 = service.getById(1, 1);
        assertThat(result2.getDescription(), equalTo(makeRequest(1).getDescription()));
        assertTrue(result2.getItems().isEmpty());
    }

    @Test
    void testGetByIdUserNotFound() {
        when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(1, 1));
    }

    @Test
    void testGetByIdRequestNotFound() {
        when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(new User()));
        when(mockRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(1, 1));
    }

    private ItemRequest makeRequest(Integer id) {
        return new ItemRequest(id, "test_desc" + id, null, moment);
    }

    private List<ItemRequest> makeRequestsList() {
        List<ItemRequest> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(makeRequest(i));
        }
        return list;
    }

    private ItemRequestShort makeShortRequest() {
        return new ItemRequestShort("test_short");
    }

    private User makeUser(Integer id) {
        return new User(id, "testUser" + id, "test" + id + "@ya.ru");
    }

    private Item makeItem(Integer id) {
        return new Item(id, "item" + id, "test_item", Boolean.TRUE, null, null);
    }

    private List<Item> makeItemsList() {
        List<Item> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Item item = makeItem(i);
            item.setRequestId(i);
            list.add(item);
        }
        return list;
    }
}
