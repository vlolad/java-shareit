package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.UserMapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ItemMapper.class})
public interface BookingMapper {

    BookingDto toDto(Booking booking);

    Booking toEntity(BookingDto bookingDto);
    @Mapping(target = "bookerId", source = "booker.id")
    BookingDtoShort toDtoShort(Booking booking);

    @Mapping(target = "item.id", source = "itemId")
    Booking toEntityFromRequest(BookingRequest request);

    List<BookingDto> toDtoList(List<Booking> bookings);
}
