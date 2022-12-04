package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.BadCommentException;
import ru.practicum.shareit.item.exception.ItemBadRequestException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository, UserRepository userRepository,
                       ItemMapper itemMapper, BookingRepository bookingRepository,
                       BookingMapper bookingMapper, CommentMapper commentMapper,
                       CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.itemMapper = itemMapper;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public ItemDto create(CreateItemRequest item, Integer ownerId) {
        Optional<User> owner = userRepository.findById(ownerId);
        if (owner.isEmpty()) throw new NotFoundException("Owner not found.");
        Item newItem = itemMapper.toEntity(item);
        newItem.setOwner(owner.get());
        log.debug("Saving new item: {}", newItem);
        return itemMapper.toDto(itemRepository.save(newItem));
    }

    @Transactional
    public ItemDto patchItem(CreateItemRequest itemDto, Integer ownerId) {
        Optional<Item> oldItem = itemRepository.findById(itemDto.getId());
        if (oldItem.isEmpty()) throw new NotFoundException("Item with ID: " + itemDto.getId() + " not found.");
        if (!Objects.equals(oldItem.get().getOwner().getId(), ownerId))
            throw new ItemBadRequestException(HttpStatus.FORBIDDEN, "Restricted PATCH: user not owner.");
        Item item = patchItem(oldItem.get(), itemDto);
        log.debug("Saving updated item: {}", item);
        return itemMapper.toDto(item);
    }

    @Transactional(readOnly = true)
    public ItemDto getItem(Integer itemId, Integer requesterId) {
        Optional<Item> item = itemRepository.findById(itemId);
        if (item.isEmpty()) throw new NotFoundException("Item with ID: " + itemId + " not found.");
        log.debug("Item with ID: {} found successfully.", itemId);
        ItemDto itemDto = itemMapper.toDto(item.get());
        addComments(itemDto);
        if (!item.get().getOwner().getId().equals(requesterId)) {
            return itemDto;
        }
        return addBookings(itemDto);
    }

    @Transactional(readOnly = true)
    public List<ItemDto> getAllByOwner(Integer ownerId, Integer from, Integer size) {
        if (userRepository.findById(ownerId).isEmpty())
            throw new NotFoundException("Owner (id: " + ownerId + ") not found.");
        log.info("Found owner (id:{}), return items.", ownerId);
        Pageable page = PageRequest.of(from / size, size);
        List<ItemDto> itemsList = itemMapper.toDtoList(itemRepository.findByOwnerId(ownerId, page));
        addBookings(itemsList);
        addComments(itemsList);
        return itemsList;
    }

    @Transactional(readOnly = true)
    public List<ItemDto> search(String text, Integer from, Integer size) {
        log.debug("Searching: {}", text);
        Pageable page = PageRequest.of(from / size, size);
        return itemMapper.toDtoList(itemRepository.search(text, page).getContent());
    }

    @Transactional
    public CommentDto createComment(CommentDto commentDto, Integer authorId) {
        Optional<User> author = userRepository.findById(authorId);
        if (author.isEmpty()) throw new NotFoundException("Author not found.");
        if (checkCommentTruth(commentDto.getItemId(), authorId))
            throw new BadCommentException("Probably, comment not truly.");
        commentDto.setAuthorName(author.get().getName());
        Comment comment = commentMapper.toEntity(commentDto);
        comment.setAuthor(author.get());
        comment.setItem(itemRepository.findById(commentDto.getItemId()).get());
        comment.setCreated(LocalDateTime.now());
        return commentMapper.toDto(commentRepository.save(comment));
    }

    private Item patchItem(Item item, CreateItemRequest newItem) {
        if (!Objects.equals(newItem.getName(), null)) {
            if (!newItem.getName().isBlank()) {
                item.setName(newItem.getName());
            }
        }
        if (!Objects.equals(newItem.getDescription(), null)) {
            if (!newItem.getDescription().isBlank()) {
                item.setDescription(newItem.getDescription());
            }
        }
        if (!Objects.equals(newItem.getAvailable(), null)) {
            item.setAvailable(newItem.getAvailable());
        }
        if (!Objects.equals(newItem.getRequestId(), null)) {
            item.setRequestId(newItem.getRequestId());
        }
        return item;
    }

    private ItemDto addBookings(ItemDto item) {
        LocalDateTime moment = LocalDateTime.now();
        item.setLastBooking(bookingMapper.toDtoShort(bookingRepository.findByItemIdAndEndIsBefore(item.getId(), moment)));
        item.setNextBooking(bookingMapper.toDtoShort(bookingRepository.findByItemIdAndStartIsAfter(item.getId(), moment)));
        return item;
    }

    private List<ItemDto> addBookings(List<ItemDto> items) {
        List<Integer> itemsIds = items.stream().map(ItemDto::getId).collect(Collectors.toList());
        List<BookingDto> shortBookings = bookingMapper.toDtoList(
                bookingRepository.findByItemIdInAndStatusIs(itemsIds,
                        BookingStatus.APPROVED, Sort.by("start").descending()));
        if (shortBookings.isEmpty()) return items;
        Map<Integer, List<BookingDtoShort>> bookingsMap = new HashMap<>();
        for (BookingDto booking : shortBookings) {
            final List<BookingDtoShort> bookingsByItemId = bookingsMap
                    .computeIfAbsent(booking.getItemId(), k -> new ArrayList<>());
            bookingsByItemId.add(bookingMapper.dtoToDtoShort(booking));
        }
        LocalDateTime moment = LocalDateTime.now();
        return items.stream().peek(item -> {
            List<BookingDtoShort> bookings = bookingsMap.getOrDefault(item.getId(), new ArrayList<>());
            if (bookings.isEmpty()) return;
            bookings = bookings.stream().sorted(Comparator.comparing(BookingDtoShort::getEnd)).collect(Collectors.toList());
            //Поменял логику поиска
            List<BookingDtoShort> previousBookings = bookings.stream().filter(b -> b.getEnd().isBefore(moment))
                    .sorted(Comparator.comparing(BookingDtoShort::getEnd)).collect(Collectors.toList());
            if (!previousBookings.isEmpty()) item.setLastBooking(previousBookings.get(0));
            List<BookingDtoShort> futureBookings = bookings.stream().filter(b -> b.getStart().isAfter(moment))
                    .sorted(Comparator.comparing(BookingDtoShort::getStart).reversed()).collect(Collectors.toList());
            if (!futureBookings.isEmpty()) item.setNextBooking(futureBookings.get(0));
        }).collect(Collectors.toList());
    }

    private ItemDto addComments(ItemDto item) {
        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId());
        item.setComments(commentMapper.toDtoList(comments));
        return item;
    }

    private List<ItemDto> addComments(List<ItemDto> items) {
        List<Integer> itemsIds = items.stream().map(ItemDto::getId).collect(Collectors.toList());
        List<CommentDto> comments = commentMapper.toDtoList(commentRepository
                .findAllByItemIdInOrderByCreatedDesc(itemsIds));
        if (comments == null || comments.isEmpty()) return items;

        Map<Integer, List<CommentDto>> commentsMap = new HashMap<>();
        for (CommentDto comment : comments) {
            commentsMap.putIfAbsent(comment.getItemId(), new ArrayList<>());
            commentsMap.get(comment.getItemId()).add(comment);
        }
        return items.stream().peek(i -> i.setComments(commentsMap.get(i.getId()))).collect(Collectors.toList());
    }

    private boolean checkCommentTruth(Integer itemId, Integer authorId) {
        List<Booking> allBookings = bookingRepository.findAllByBookerIdAndItemIdAndEndIsBefore(authorId,
                itemId, LocalDateTime.now());
        allBookings = allBookings.stream().filter(b -> b.getStatus().equals(BookingStatus.APPROVED))
                .collect(Collectors.toList());
        return allBookings.isEmpty();
    }
}
