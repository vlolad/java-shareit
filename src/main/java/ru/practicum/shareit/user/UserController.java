package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.exception.UserCreationException;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private int id = 0;
    private final InMemoryUserStorage userStorage;

    @Autowired
    public UserController(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody @Valid User user) {
        log.debug("POST-request at /users");
        if (user.getEmail() == null) {
            log.error("Email is NULL, it cant be null.");
            throw new UserCreationException(HttpStatus.BAD_REQUEST, "Email is NULL.");
        }
        if (checkEmail(user)) {
            log.debug("Creating user with email: {}", user.getEmail());
            user.setId(generateId());
            userStorage.get().put(user.getId(), user);
            log.info("User successfully created.");
            log.debug(user.toString());
            return user;
        } else {
            log.error("Email already registered.");
            throw new UserCreationException("User with such Email already exists.");
        }
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public User patchUser(@RequestBody User user,
                          @PathVariable Integer userId) {
        log.debug("PATCH-request at /users/{}", userId);
        Optional<User> oldUser = Optional.ofNullable(userStorage.get().get(userId));
        if (oldUser.isPresent()) {
            log.debug("User with id: {} exists.", userId);
            if (checkEmail(user) || (user.getEmail().equals(oldUser.get().getEmail()))) {
                User patchedUser = userStorage.get().get(userId).updateUser(user);
                log.info("User with id: {} successfully patched.", userId);
                log.debug(patchedUser.toString());
                return patchedUser;
            } else {
                log.error("Email already registered.");
                throw new UserCreationException("User with such Email already exists.");
            }
        }
        return null;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllUsers() {
        log.debug("GET-request at /users");
        return new ArrayList<>(userStorage.get().values());
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public User getUser(@PathVariable Integer userId) {
        log.debug("GET-request at /users/{}", userId);
        return userStorage.get().get(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteUser(@PathVariable Integer userId) {
        log.warn("DELETE-request at /users/{}", userId);
        userStorage.get().remove(userId);
    }

    private Integer generateId() {
        log.debug("Generating id...");
        return ++id;
    }

    private boolean checkEmail(User user) {
        Set<String> emails = userStorage.get().values().stream()
                .map(User::getEmail).collect(Collectors.toSet());
        return !emails.contains(user.getEmail());
    }
}
