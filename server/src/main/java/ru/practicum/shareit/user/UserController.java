package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.DeleteResponse;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping //create
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody UserDto user) {
        log.debug("POST-request at /users");
        return userService.create(user);
    }

    @PatchMapping("/{userId}") //editUser
    @ResponseStatus(HttpStatus.OK)
    public UserDto patch(@RequestBody UserDto user,
                         @PathVariable Integer userId) {
        log.debug("PATCH-request at /users/{}", userId);
        user.setId(userId);
        return userService.patch(user);
    }

    @GetMapping //getAll
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAll() {
        log.debug("GET-request at /users");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}") //getById
    @ResponseStatus(HttpStatus.OK)
    public UserDto getById(@PathVariable Integer userId) {
        log.debug("GET-request at /users/{}", userId);
        return userService.getUser(userId);
    }

    @DeleteMapping("/{userId}") //deleteById
    public DeleteResponse delete(@PathVariable Integer userId) {
        log.warn("DELETE-request at /users/{}", userId);
        userService.deleteUser(userId);
        return new DeleteResponse(userId, LocalDateTime.now(), "Deleted successfully");
    }
}
