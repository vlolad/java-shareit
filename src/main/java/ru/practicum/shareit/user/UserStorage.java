package ru.practicum.shareit.user;

import java.util.Map;

public interface UserStorage {

    void put(Integer key, User value);

    User get(Integer key);

    Map<Integer, User> getAll();

    void replace(Integer key, User value);

    User remove(Integer key);

    boolean containsKey(Integer key);
}
