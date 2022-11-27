package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDtoShort {
    private Integer id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
    private Integer bookerId;
}
