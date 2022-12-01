package ru.practicum.shareit.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.user.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserCreationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserTests {

    @Mock
    UserRepository mockUserRepo;
    UserMapper userMapper = new UserMapperImpl();
    UserService service;

    @Test
    void testUserMapper() {
        User user = makeUser(1);
        UserDto userDto = userMapper.toDto(user);
        assertThat(user.getId(), equalTo(userDto.getId()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        User userBack = userMapper.toEntity(userDto);
        assertThat(user.getId(), equalTo(userBack.getId()));
        assertThat(user.getEmail(), equalTo(userBack.getEmail()));
        assertThat(user.getName(), equalTo(userBack.getName()));
        assertEquals(user, userBack);
    }

    @BeforeEach
    void makeService() {
        service = new UserService(mockUserRepo, userMapper);
    }

    @Test
    void testUserCreate() {
        UserDto newUser = userMapper.toDto(makeUser(2));
        Mockito.when(mockUserRepo.save(Mockito.any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        UserDto savedUser = service.create(newUser);
        assertThat(newUser, equalTo(savedUser));
    }

    @Test
    void testPatchUser() {
        User user = makeUser(1);
        Mockito.when(mockUserRepo.findById(1)).thenReturn(Optional.of(user));
        Mockito.when(mockUserRepo.findUserByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        UserDto newUser = new UserDto();
        newUser.setId(1);
        newUser.setName("notTestUser");
        newUser.setEmail(user.getEmail());
        UserDto resultUser = service.patch(newUser);
        assertThat(resultUser, equalTo(newUser));

        UserDto newUser2 = new UserDto();
        newUser2.setId(1);
        newUser2.setName("notTestUser");
        newUser2.setEmail("test49@ya.ru");
        UserDto resultUser2 = service.patch(newUser2);
        assertThat(resultUser2, equalTo(newUser2));
    }

    @Test
    void testPatchUserNotFound() {
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.patch(userMapper.toDto(makeUser(7))));
    }

    @Test
    void testPatchUserEmailAlreadyRegistered() {
        User user = makeUser(2);
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(makeUser(3)));
        Mockito.when(mockUserRepo.findUserByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        UserDto newUser = new UserDto(3, "testing", user.getEmail());
        assertThrows(UserCreationException.class, () -> service.patch(newUser));
    }

    @Test
    void testGetAllUsers() {
        List<UserDto> expected = userMapper.toDtoList(makeUsersList());
        Mockito.when(mockUserRepo.findAll()).thenReturn(makeUsersList());
        List<UserDto> result = service.getAllUsers();
        assertThat(expected, equalTo(result));
    }

    @Test
    void testGetUser() {
        User user = makeUser(2);
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.of(user));
        assertThat(userMapper.toDto(user), equalTo(service.getUser(2)));
    }

    @Test
    void testGetUserNotFound() {
        Mockito.when(mockUserRepo.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getUser(1));
    }

    private User makeUser(Integer id) {
        return new User(id, "testUser" + id, "test" + id + "@ya.ru");
    }

    private List<User> makeUsersList() {
        List<User> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(makeUser(i));
        }
        return list;
    }

}
