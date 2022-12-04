package ru.practicum.shareit.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShort;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class ItemRequestService {

    private final ItemRequestRepository repository;
    private final UserRepository userRepository;
    private final ItemRequestMapper mapper;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Autowired
    public ItemRequestService(ItemRequestRepository repository, UserRepository userRepository,
                              ItemRepository itemRepository, ItemRequestMapper mapper,
                              ItemMapper itemMapper) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
    }

    @Transactional
    public ItemRequestDto create(ItemRequestShort request, Integer userId) {
        User user = findUser(userId);

        ItemRequest itemRequest = mapper.toEntity(request);
        itemRequest.setRequester(user);
        itemRequest.setCreated(LocalDateTime.now());
        log.debug("Saving new item request: {}", itemRequest.getDescription());
        return mapper.toDto(repository.save(itemRequest));
    }

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getByUser(Integer userId) {
        findUser(userId); //Просто проверка, т.к. не требуется никуда его передавать.
        Sort sort = Sort.by("created").ascending();
        return addItems(mapper.toListDto(repository.findAllByRequesterId(userId, sort)));
    }

    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAll(Integer userId, Integer from, Integer size) {
        findUser(userId);
        Pageable page = PageRequest.of(from / size, size, Sort.by("created").ascending());
        List<ItemRequestDto> requests = mapper.toListDto(
                repository.findAllByRequesterIdNot(userId, page).getContent());
        return addItems(requests);
    }

    @Transactional(readOnly = true)
    public ItemRequestDto getById(Integer userId, Integer requestId) {
        findUser(userId);
        Optional<ItemRequest> request = repository.findById(requestId);
        if (request.isEmpty()) throw new NotFoundException("Request not found.");
        return addItems(mapper.toDto(request.get()));
    }

    private User findUser(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new NotFoundException("Owner not found.");
        return user.get();
    }

    private List<ItemRequestDto> addItems(List<ItemRequestDto> requests) {
        List<Integer> requestsId = requests.stream().map(ItemRequestDto::getId).collect(toList());
        List<ItemDto> items = itemMapper.toDtoList(itemRepository.findAllByRequestIdIn(requestsId));
        if (items == null || items.isEmpty()) return requests;
        Map<Integer, List<ItemDto>> itemsMap = items.stream().collect(groupingBy(ItemDto::getRequestId, toList()));
        return requests.stream().peek(r -> r.setItems(itemsMap.getOrDefault(r.getId(), Collections.emptyList())))
                .collect(toList());
    }

    private ItemRequestDto addItems(ItemRequestDto request) {
        List<ItemDto> items = itemMapper.toDtoList(itemRepository.findAllByRequestId(request.getId()));
        if (items == null || items.isEmpty()) return request;
        request.setItems(items);
        return request;
    }
}
