package ru.practicum.shareit.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.dto.UserDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toUser(UserDto user) {
        return new User(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
