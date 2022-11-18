package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    Item toEntity(ItemDto itemDto);
    ItemDto toDto(Item item);
    List<ItemDto> toDtoList(List<Item> items);
}
