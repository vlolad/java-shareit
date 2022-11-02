package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> userStorage = new HashMap<>();

    public void put(Integer key, User value) {
        log.info("Put on key={}: {}", key, value);
        userStorage.put(key, value);
    }

    public User get(Integer key) {
        log.info("Get: key={}", key);
        return userStorage.get(key);
    }

    public Map<Integer, User> getAll() {
        log.info("Get all storage");
        return new HashMap<>(userStorage);
    }

    public void replace(Integer key, User value) {
        log.info("Replace on key={}: {}", key, value);
        userStorage.replace(key, value);
    }

    public User remove(Integer key) {
        log.info("Remove key={}", key);
        return userStorage.remove(key);
    }

    public boolean containsKey(Integer key) {
        log.info("ContainsKey key={}", key);
        return userStorage.containsKey(key);
    }
}
