package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserCreationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private int id = 0;
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto createUser(UserDto user) {
        if (checkEmail(user)) {
            log.debug("Creating user with email: {}", user.getEmail());
            user.setId(generateId());
            userStorage.get().put(user.getId(), UserMapper.toUser(user));
            log.info("User successfully created >>>");
            log.debug(user.toString());
            return user;
        } else {
            log.error("Email already registered.");
            throw new UserCreationException("User with such Email already exists.");
        }
    }

    public UserDto patchUser(UserDto user) {
        Optional<User> oldUser = Optional.ofNullable(userStorage.get().get(user.getId()));
        if (oldUser.isPresent()) {
            log.debug("User with id: {} exists.", user.getId());
            if (checkEmail(user) || (user.getEmail().equals(oldUser.get().getEmail()))) {
                User patchedUser = updateUser(oldUser.get(), user);
                userStorage.get().replace(user.getId(), patchedUser);
                log.info("User with id: {} successfully patched.", patchedUser.getId());
                log.debug(patchedUser.toString());
                return UserMapper.toUserDto(patchedUser);
            } else {
                log.error("Email already registered.");
                throw new UserCreationException("User with such Email already exists.");
            }
        } else {
            log.warn("User with such ID not found.");
            throw new NotFoundException("User with ID: " + user.getId() + " not found.");
        }
    }

    public List<UserDto> getAllUsers() {
        return userStorage.get().values().stream()
                .map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    public UserDto getUser(Integer userId) {
        return UserMapper.toUserDto(userStorage.get().get(userId));
    }

    public boolean deleteUser(Integer userId) {
        return Optional.ofNullable(userStorage.get().remove(userId)).isPresent();
    }

    private User updateUser(User user, UserDto update) {
        if (update.getName() != null) {
            user.setName(update.getName());
        }
        if (update.getEmail() != null) {
            user.setEmail(update.getEmail());
        }
        return user;
    }

    private Integer generateId() {
        log.debug("Generating id...");
        return ++id;
    }

    private boolean checkEmail(UserDto user) {
        Set<String> emails = userStorage.get().values().stream()
                .map(User::getEmail).collect(Collectors.toSet());
        return !emails.contains(user.getEmail());
    }
}
