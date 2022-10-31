package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> userStorage = new HashMap<>();

    public Map<Integer, User> get() {
        return userStorage;
    }
}
