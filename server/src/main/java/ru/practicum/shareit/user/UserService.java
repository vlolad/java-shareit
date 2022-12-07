package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.handler.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userStorage;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userStorage, UserMapper userMapper) {
        this.userStorage = userStorage;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserDto create(UserDto user) {
        log.debug("Creating user with email: {}", user.getEmail());
        return userMapper.toDto(userStorage.save(userMapper.toEntity(user)));
    }

    @Transactional
    public UserDto patch(UserDto user) {
        Optional<User> oldUser = userStorage.findById(user.getId());
        if (oldUser.isPresent()) {
            log.debug("User with id: {} exists.", user.getId());
            User patchedUser = patchUser(oldUser.get(), user);
            return userMapper.toDto(patchedUser);
        } else {
            log.warn("User with such ID not found.");
            throw new NotFoundException("User with ID: " + user.getId() + " not found.");
        }
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userMapper.toDtoList(userStorage.findAll());
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Integer userId) {
        Optional<User> user = userStorage.findById(userId);
        if (user.isPresent()) {
            return userMapper.toDto(user.get());
        } else {
            log.warn("User with such ID not found.");
            throw new NotFoundException("User with ID: " + userId + " not found.");
        }
    }

    @Transactional
    public void deleteUser(Integer userId) {
        userStorage.deleteById(userId);
    }

    private User patchUser(User user, UserDto update) {
        if (update.getName() != null) {
            if (!update.getName().isBlank()) {
                user.setName(update.getName());
            }
        }
        if (update.getEmail() != null) {
            if (!update.getEmail().isBlank()) {
                user.setEmail(update.getEmail());
            }
        }
        return user;
    }
}
