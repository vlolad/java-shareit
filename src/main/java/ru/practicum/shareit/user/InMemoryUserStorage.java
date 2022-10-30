package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage {

    private final Map<Integer, User> userStorage = new HashMap<>();

    public Map<Integer, User> get() {
        return userStorage;
    }
}
