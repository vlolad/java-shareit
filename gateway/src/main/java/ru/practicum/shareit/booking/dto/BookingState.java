package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.handler.model.BookingBadRequest;

public enum BookingState {
    // Все
    ALL,
    // Текущие
    CURRENT,
    // Будущие
    FUTURE,
    // Завершенные
    PAST,
    // Отклоненные
    REJECTED,
    // Ожидающие подтверждения
    WAITING;

    public static BookingState parseState(String line) {
        BookingState state;
        try {
            state = BookingState.valueOf(line.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BookingBadRequest("Unknown state: " + line, "Only our types allowed.");
        }
        return state;
    }
}
