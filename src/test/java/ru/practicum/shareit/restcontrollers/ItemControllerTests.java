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
import ru.practicum.shareit.handler.exception.NotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateItemRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.BadCommentException;
import ru.practicum.shareit.item.exception.ItemBadRequestException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTests {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemService service;
    @Autowired
    private MockMvc mvc;
    ItemMapper itemMapper = new ItemMapperImpl();
    LocalDateTime moment = LocalDateTime.now();

    @Test
    void testCheckMapper() {
        CreateItemRequest request = makeRequest(1);
        ItemDto result = makeItemDto(1);
        assertEquals(result, requestToItemDto(request));
    }

    @Test
    void testCreateItem() throws Exception {
        CreateItemRequest request = makeRequest(1);
        when(service.create(Mockito.any(CreateItemRequest.class), Mockito.anyInt()))
                .thenReturn(requestToItemDto(request));
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(request.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(request.getName()), String.class))
                .andExpect(jsonPath("$.description", is(request.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(Boolean.TRUE), Boolean.class));
    }

    @Test
    void testCreateItemNameIsBlank() throws Exception {
        CreateItemRequest request = makeRequest(2);
        request.setName("   ");
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateItemDescriptionIsBlank() throws Exception {
        CreateItemRequest request = makeRequest(2);
        request.setDescription("   ");
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateItemAvailableIsNull() throws Exception {
        CreateItemRequest request = makeRequest(2);
        request.setAvailable(null);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testPatchItem() throws Exception {
        CreateItemRequest request = makeRequest(3);
        ItemDto expected = requestToItemDto(request);
        request.setId(1);
        when(service.patchItem(Mockito.any(CreateItemRequest.class), Mockito.anyInt()))
                .thenReturn(expected);
        mvc.perform(patch("/items/3")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(expected.getId()), Integer.class))
                .andExpect(jsonPath("$.name", is(expected.getName()), String.class))
                .andExpect(jsonPath("$.description", is(expected.getDescription()), String.class))
                .andExpect(jsonPath("$.available", is(expected.getAvailable()), Boolean.class));
    }

    @Test
    void testPatchItemNotOwner() throws Exception {
        when(service.patchItem(Mockito.any(CreateItemRequest.class), Mockito.anyInt()))
                .thenThrow(ItemBadRequestException.class);
        mvc.perform(patch("/items/3")
                        .content(mapper.writeValueAsString(makeRequest(1)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(403));
    }

    @Test
    void testGetItem() throws Exception {
        when(service.getItem(eq(5), Mockito.anyInt()))
                .thenReturn(makeItemDto(5));
        mvc.perform(get("/items/5")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5), Integer.class));
    }

    @Test
    void testGetItemNotFound() throws Exception {
        when(service.getItem(eq(5), Mockito.anyInt()))
                .thenThrow(NotFoundException.class);
        mvc.perform(get("/items/5")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(404));
    }

    @Test
    void testGetAll() throws Exception {
        when(service.getAllByOwner(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(makeItemDtoList());
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void testGetAllSizeIsNull() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllSizeIsNegative() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllFromIsNegative() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllSizeAndFromIsNegative() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1")
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testSearch() throws Exception {
        when(service.search(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(makeItemDtoList());
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "ok")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void testSearchTextBlank() throws Exception {
        when(service.search(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(makeItemDtoList());
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "   ")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testSearchSizeIsNull() throws Exception {
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "ok")
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testSearchSizeIsNegative() throws Exception {
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "ok")
                        .param("size", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testSearchFromIsNegative() throws Exception {
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "ok")
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testSearchSizeAndFromIsNegative() throws Exception {
        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "ok")
                        .param("size", "-1")
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateComment() throws Exception {
        CommentDto comment = makeCommentDto(1);
        when(service.createComment(Mockito.any(CommentDto.class), Mockito.anyInt()))
                .thenReturn(comment);
        CommentDto request = makeCommentDto(1);
        request.setId(null);
        mvc.perform(post("/items/2/comment")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(comment.getId()), Integer.class))
                .andExpect(jsonPath("$.text", is(comment.getText()), String.class))
                .andExpect(jsonPath("$.itemId", is(comment.getItemId()), Integer.class));
    }

    @Test
    void testCreateCommentTextIsBlank() throws Exception {
        CommentDto comment = makeCommentDto(1);
        comment.setId(null);
        comment.setText("            ");
        mvc.perform(post("/items/2/comment")
                        .content(mapper.writeValueAsString(comment))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateNotTrulyComment() throws Exception {
        when(service.createComment(Mockito.any(CommentDto.class), Mockito.anyInt()))
                .thenThrow(BadCommentException.class);
        mvc.perform(post("/items/2/comment")
                        .content(mapper.writeValueAsString(makeCommentDto(1)))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    private CreateItemRequest makeRequest(Integer id) {
        return new CreateItemRequest(id, "test_item" + id, "test_desc", Boolean.TRUE, null);
    }

    private ItemDto makeItemDto(Integer id) {
        return new ItemDto(id, "test_item" + id, "test_desc", Boolean.TRUE,
                null, null, null, null);
    }

    private List<ItemDto> makeItemDtoList() {
        List<ItemDto> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(makeItemDto(i));
        }
        return list;
    }

    private ItemDto requestToItemDto(CreateItemRequest request) {
        return itemMapper.toDto(itemMapper.toEntity(request));
    }

    private CommentDto makeCommentDto(Integer id) {
        return new CommentDto(id, "test_comment" + id, id + 1, "author" + id, moment);
    }
}
