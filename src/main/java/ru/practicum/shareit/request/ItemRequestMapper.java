package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShort;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    ItemRequest toEntity(ItemRequestShort itemRequestShort);

    @Mapping(target = "items", ignore = true)
    ItemRequestDto toDto(ItemRequest request);

    List<ItemRequestDto> toListDto(List<ItemRequest> requests);
}
