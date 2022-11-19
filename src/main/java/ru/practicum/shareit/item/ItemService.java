package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    public ItemDto create(ItemDto item, Integer ownerId) {
        Optional<User> owner = userRepository.findById(ownerId);
        if (owner.isEmpty()) throw new NotFoundException("Owner not found.");
        Item newItem = itemMapper.toEntity(item);
        newItem.setOwner(owner.get());
        log.debug("Saving new item: {}", newItem);
        return itemMapper.toDto(itemRepository.save(newItem));
    }

    public ItemDto patchItem(ItemDto itemDto, Integer ownerId) {
        Optional<Item> oldItem = itemRepository.findById(itemDto.getId());
        if (oldItem.isEmpty()) throw new NotFoundException("Item with ID: " + itemDto.getId() + " not found.");
        if (!Objects.equals(oldItem.get().getOwner().getId(), ownerId))
            throw new ItemBadRequestException(HttpStatus.FORBIDDEN, "Restricted PATCH: user not owner.");
        Item item = patchItem(oldItem.get(), itemDto);
        log.debug("Saving updated item: {}", item);
        return itemMapper.toDto(itemRepository.save(item));
    }

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

    public List<ItemDto> getAllItemsByOwner(Integer ownerId) {
        if (userRepository.findById(ownerId).isEmpty())
            throw new NotFoundException("Owner (id: " + ownerId + ") not found.");
        log.info("Found owner (id:{}), return items.", ownerId);
        List<ItemDto> itemsList = itemMapper.toDtoList(itemRepository.findByOwner_id(ownerId));
        return itemsList.stream().peek(this::addBookings).collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text) {
        log.debug("Searching: {}", text);
        return itemMapper.toDtoList(itemRepository.search(text));
    }

    public CommentDto createComment (CommentDto commentDto, Integer authorId) {
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

    private Item patchItem(Item item, ItemDto newItem) {
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
        return item;
    }

    private ItemDto addBookings(ItemDto item) {
        LocalDateTime moment = LocalDateTime.now();
        item.setLastBooking(bookingMapper.toDtoShort(bookingRepository
                .findByItemIdAndEndIsBefore(item.getId(), moment)));
        item.setNextBooking(bookingMapper.toDtoShort(bookingRepository
                .findByItemIdAndStartIsAfter(item.getId(), moment)));
        return item;
    }

    private ItemDto addComments(ItemDto item) {
        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId());
        item.setComments(commentMapper.toDtoList(comments));
        return item;
    }

    private boolean checkCommentTruth(Integer itemId, Integer authorId) {
        List<Booking> allBookings = bookingRepository.findAllByBookerIdAndItemIdAndEndIsBefore(
                authorId, itemId, LocalDateTime.now());
        allBookings = allBookings.stream().filter(b -> b.getStatus().equals(BookingStatus.APPROVED))
                .collect(Collectors.toList());
        return allBookings.isEmpty();
    }
}
