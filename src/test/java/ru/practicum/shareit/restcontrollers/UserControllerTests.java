package ru.practicum.shareit.restcontrollers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserCreationException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTests {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    UserService service;
    @Autowired
    private MockMvc mvc;

    @Test
    void testCreateUser() throws Exception {
        UserDto userDto = makeUserDto(1);
        when(service.create(Mockito.any(UserDto.class))).thenReturn(userDto);
        UserDto createUserRequest = makeUserDto(1);
        createUserRequest.setId(null);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(createUserRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1), Integer.class))
                .andExpect(jsonPath("$.name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail()), String.class));
    }

    @Test
    void testCreateUserIdNotNull() throws Exception {
        UserDto userDto = makeUserDto(1);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateUserBlankName() throws Exception {
        UserDto userDto = makeUserDto(1);
        userDto.setId(null);
        userDto.setName("  ");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateUserBadEmail() throws Exception {
        UserDto userDto = makeUserDto(1);
        userDto.setId(null);
        userDto.setEmail("heh.com@");
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateUserNullEmail() throws Exception {
        UserDto userDto = makeUserDto(1);
        userDto.setId(null);
        userDto.setEmail(null);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateUserEmailAlreadyExists() throws Exception {
        when(service.create(Mockito.any(UserDto.class))).thenThrow(UserCreationException.class);
        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(makeUserDto(1)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testPatchUser() throws Exception {
        UserDto patchRequest = makeUserDto(2);
        patchRequest.setId(1);
        when(service.patch(Mockito.any(UserDto.class))).thenReturn(makeUserDto(2));
        mvc.perform(patch("/users/2")
                        .content(mapper.writeValueAsString(patchRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2), Integer.class))
                .andExpect(jsonPath("$.name", is(patchRequest.getName()), String.class))
                .andExpect(jsonPath("$.email", is(patchRequest.getEmail()), String.class));
    }

    @Test
    void testPatchUserBadEmail() throws Exception {
        UserDto userDto = makeUserDto(2);
        userDto.setEmail("@@@asked?");
        mvc.perform(patch("/users/2")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAll() throws Exception {
        when(service.getAllUsers()).thenReturn(makeUserDtoList());
        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void testGetById() throws Exception {
        UserDto userDto = makeUserDto(1);
        when(service.getUser(Mockito.anyInt())).thenReturn(userDto);
        mvc.perform(get("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1), Integer.class))
                .andExpect(jsonPath("$.name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail()), String.class));
    }

    @Test
    void testDelete() throws Exception {
        mvc.perform(delete("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private UserDto makeUserDto(Integer id) {
        return new UserDto(id, "test_user" + id, "test" + id + "@ya.ru");
    }

    private List<UserDto> makeUserDtoList() {
        List<UserDto> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(makeUserDto(i));
        }
        return list;
    }
}
