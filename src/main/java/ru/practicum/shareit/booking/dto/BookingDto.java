package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Integer id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;
    private MiniItem item;
    private MiniBooker booker;

    public Integer getItemId() {
        return item.getId();
    }

    public Integer getBookerId() {
        return booker.getId();
    }

    @Data
    public static class MiniBooker {
        private final Integer id;
        private final String name;
    }

    @Data
    public static class MiniItem {
        private final Integer id;
        private final String name;
    }
}
