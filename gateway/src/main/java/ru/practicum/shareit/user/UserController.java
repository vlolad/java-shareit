package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validate.Create;
import ru.practicum.shareit.validate.Update;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Validated(Create.class) UserDto user) {
        log.info("Create new user: {}", user.toString());
        return userClient.create(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> editUser(@RequestBody @Validated(Update.class) UserDto user,
                                           @PathVariable Integer userId) {
        log.info("Patch user id={}", userId);
        return userClient.editUser(userId, user);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Get all users.");
        return userClient.getAll();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable Integer userId) {
        log.info("Get user by id={}", userId);
        return userClient.getById(userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteById(@PathVariable Integer userId) {
        log.warn("Delete user with id={}", userId);
        return userClient.deleteById(userId);
    }
}
