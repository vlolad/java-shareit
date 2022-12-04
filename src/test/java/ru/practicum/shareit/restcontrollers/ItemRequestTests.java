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
import ru.practicum.shareit.request.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShort;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestTests {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemRequestService service;
    @Autowired
    private MockMvc mvc;
    ItemRequestMapper itemRequestMapper = new ItemRequestMapperImpl();
    LocalDateTime moment = LocalDateTime.now();

    @Test
    void testCheckMapper() {
        ItemRequestDto expected = makeDto(1);
        expected.setId(null);
        expected.setCreated(null);
        ItemRequestShort shortRequest = makeShort(1);
        assertEquals(expected, requestToDto(shortRequest));
    }

    @Test
    void testCreateItemRequest() throws Exception {
        ItemRequestDto requestDto = makeDto(1);
        when(service.create(Mockito.any(ItemRequestShort.class), Mockito.anyInt())).thenReturn(requestDto);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(makeShort(1)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Integer.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription()), String.class))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void testCreateItemRequestNegativeUserId() throws Exception {
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(makeShort(1)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", -1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateItemRequestNull() throws Exception {
        ItemRequestShort request = makeShort(1);
        request.setDescription(null);
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", -1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateItemRequestBlank() throws Exception {
        ItemRequestShort request = makeShort(1);
        request.setDescription("     ");
        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", -1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetByUser() throws Exception {
        when(service.getByUser(1)).thenReturn(makeDtoList());
        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void testGetByUserNegativeId() throws Exception {
        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", -1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAll() throws Exception {
        when(service.getAll(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(makeDtoList());
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void testGetAllFromIsNegative() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllSizeIsNegative() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllSizeIsZero() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllSizeAndFromIsNegative() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1")
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllUserIsNegative() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", -1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetById() throws Exception {
        when(service.getById(Mockito.anyInt(), Mockito.anyInt())).thenReturn(makeDto(1));
        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void testGetByIdUserIsNegative() throws Exception {
        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", -1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetByIdRequestIsNegative() throws Exception {
        mvc.perform(get("/requests/-1")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    private ItemRequestShort makeShort(Integer num) {
        return new ItemRequestShort("desc" + num);
    }

    private ItemRequestDto makeDto(Integer id) {
        return new ItemRequestDto(id, "desc" + id, moment.plusDays(id), new ArrayList<>());
    }

    private List<ItemRequestDto> makeDtoList() {
        List<ItemRequestDto> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(makeDto(i));
        }
        return list;
    }

    private ItemRequestDto requestToDto(ItemRequestShort request) {
        return itemRequestMapper.toDto(itemRequestMapper.toEntity(request));
    }
}
