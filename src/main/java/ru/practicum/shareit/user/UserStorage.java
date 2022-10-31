package ru.practicum.shareit.user;

import java.util.Map;

public interface UserStorage {

    Map<Integer, User> get();
}
