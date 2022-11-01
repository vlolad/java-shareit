package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid UserDto user) {
        log.debug("POST-request at /users");
        return userService.createUser(user);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto patchUser(@RequestBody UserDto user,
                          @PathVariable Integer userId) {
        log.debug("PATCH-request at /users/{}", userId);
        user.setId(userId);
        return userService.patchUser(user);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getAllUsers() {
        log.debug("GET-request at /users");
        return userService.getAllUsers();
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto getUser(@PathVariable Integer userId) {
        log.debug("GET-request at /users/{}", userId);
        return userService.getUser(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        log.warn("DELETE-request at /users/{}", userId);
        boolean del = userService.deleteUser(userId);
        if (del) {
            return new ResponseEntity<>("User (id: " + userId + ") deleted successfully.", HttpStatus.OK);
        } else {
            throw new NotFoundException("User (id: " + userId + ") not found.");
        }
    }
}
