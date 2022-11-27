package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toEntity(ItemDto itemDto);

    Item toEntity(CreateItemRequest createItemRequest);

    @Mapping(target = "comments", ignore = true)
    ItemDto toDto(Item item);

    List<ItemDto> toDtoList(List<Item> items);
}
