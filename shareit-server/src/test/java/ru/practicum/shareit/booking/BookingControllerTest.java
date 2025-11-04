package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.error.ValidationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController controller;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private BookingResponseDto responseDto;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ru.practicum.shareit.error.ErrorHandler())
                .build();

        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();

        responseDto = BookingResponseDto.builder()
                .id(1L)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void create_shouldReturn200() throws Exception {
        when(bookingService.create(anyLong(), any())).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void create_shouldReturn400WithoutHeader() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approve_shouldReturn200() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), any(Boolean.class))).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/5")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void approve_shouldReturn404WhenNotFound() throws Exception {
        when(bookingService.approve(anyLong(), anyLong(), any(Boolean.class)))
                .thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(patch("/bookings/5")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(bookingService.getById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/10")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getById_shouldReturn404WhenNotFound() throws Exception {
        when(bookingService.getById(anyLong(), anyLong())).thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(get("/bookings/10")
                        .header("X-Sharer-User-Id", 5L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllByUser_shouldReturnList() throws Exception {
        when(bookingService.getAllByUser(anyLong(), any())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllByUser_shouldReturn400ForInvalidState() throws Exception {
        when(bookingService.getAllByUser(anyLong(), any())).thenThrow(new ValidationException("Invalid state"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 7L)
                        .param("state", "BAD_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByOwner_shouldReturnList() throws Exception {
        when(bookingService.getAllByOwner(anyLong(), any())).thenReturn(List.of(responseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }
}