package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.validate.Create;
import ru.practicum.shareit.validate.Update;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@Validated
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(Create.class)
    public UserDto createUser(@RequestBody @Valid UserDto user) {
        log.debug("POST-request at /users");
        return userService.create(user);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Validated(Update.class)
    public UserDto patchUser(@RequestBody @Valid UserDto user,
                          @PathVariable Integer userId) {
        log.debug("PATCH-request at /users/{}", userId);
        user.setId(userId);
        return userService.patch(user);
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
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        log.warn("DELETE-request at /users/{}", userId);
        userService.deleteUser(userId);
        return new ResponseEntity<>("Request for delete user (id: " + userId + ")" +
                " executed successfully.", HttpStatus.OK);
    }
}
