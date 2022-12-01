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
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequest;
import ru.practicum.shareit.booking.exception.BookingBadRequest;
import ru.practicum.shareit.booking.exception.BookingStatusChangeException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTests {
    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService service;
    @Autowired
    private MockMvc mvc;
    BookingMapper bookingMapper = new BookingMapperImpl();
    LocalDateTime moment = LocalDateTime.now();

    @Test
    void testCheckBookingMapper() {
        BookingRequest request = makeRequest(1);
        BookingDto expected = makeBookingDto(1);
        expected.setId(null);
        expected.setBooker(null);
        assertEquals(expected, requestToDto(request));
    }

    @Test
    void testCreateBooking() throws Exception {
        BookingRequest request = makeRequest(1);
        when(service.create(Mockito.any(BookingRequest.class), Mockito.anyInt())).thenReturn(makeBookingDto(1));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1), Integer.class))
                .andExpect(jsonPath("$.start").isNotEmpty())
                .andExpect(jsonPath("$.end").isNotEmpty())
                .andExpect(jsonPath("$.item.id", is(1), Integer.class));
    }

    @Test
    void testCreateBookingIllegalStartTime() throws Exception {
        BookingRequest request = makeRequest(5);
        request.setStart(moment.plusYears(3));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateBookingNegativeItemId() throws Exception {
        BookingRequest request = makeRequest(5);
        request.setItemId(-2);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateBookingStartInPast() throws Exception {
        BookingRequest request = makeRequest(5);
        request.setStart(moment.minusYears(3));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateBookingEndInPast() throws Exception {
        BookingRequest request = makeRequest(5);
        request.setEnd(moment.minusYears(3));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateBookingStartIsNull() throws Exception {
        BookingRequest request = makeRequest(5);
        request.setStart(null);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testCreateBookingEndIsNull() throws Exception {
        BookingRequest request = makeRequest(5);
        request.setEnd(null);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testSetStatusByOwner() throws Exception {
        BookingDto answer = makeBookingDto(1);
        answer.setStatus("APPROVED");
        when(service.changeStatusByOwner(Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyInt())).thenReturn(answer);
        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(answer.getId()), Integer.class));
    }

    @Test
    void testSetStatusByOwnerAlreadyApproved() throws Exception {
        when(service.changeStatusByOwner(Mockito.anyInt(),
                Mockito.anyBoolean(), Mockito.anyInt())).thenThrow(BookingStatusChangeException.class);
        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));

    }

    @Test
    void testGetById() throws Exception {
        when(service.getById(Mockito.anyInt(), Mockito.anyInt())).thenReturn(makeBookingDto(1));
        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void testGetAll() throws Exception {
        when(service.getAllByUser(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt())).thenReturn(makeBookingDtoList());
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void testGetAllBadState() throws Exception {
        when(service.getAllByUser(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt())).thenThrow(BookingBadRequest.class);
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "RYHNSKDN")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllFromIsNegative() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllSizeIsNegative() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllSizeIsZero() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetAllSizeAndFromIsNegative() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1")
                        .param("from", "-9999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetOwnerBookings() throws Exception {
        when(service.getAllByUserOwner(Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyInt())).thenReturn(makeBookingDtoList());
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty());
    }

    @Test
    void testGetOwnerFromIsNegative() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetOwnerSizeIsNegative() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetOwnerSizeIsZero() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    void testGetOwnerSizeAndFromIsNegative() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("size", "-1")
                        .param("from", "-9999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }


    private BookingRequest makeRequest(Integer id) {
        return new BookingRequest(id, moment.plusHours(id), moment.plusDays(id));
    }

    private BookingDto makeBookingDto(Integer id) {
        return new BookingDto(id, moment.plusHours(id), moment.plusDays(id), null,
                new BookingDto.MiniItem(id, null), new BookingDto.MiniBooker(id + 1, null));
    }

    private List<BookingDto> makeBookingDtoList() {
        List<BookingDto> list = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            list.add(makeBookingDto(i));
        }
        return list;
    }

    private BookingDto requestToDto(BookingRequest request) {
        return bookingMapper.toDto(bookingMapper.toEntityFromRequest(request));
    }
}
