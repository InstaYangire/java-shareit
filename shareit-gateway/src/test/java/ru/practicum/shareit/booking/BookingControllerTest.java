package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void create() throws Exception {
        when(bookingClient.create(anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());

        verify(bookingClient).create(eq(1L), any());
    }

    @Test
    void approve() throws Exception {
        when(bookingClient.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/bookings/5")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        verify(bookingClient).approve(2L, 5L, true);
    }

    @Test
    void getById() throws Exception {
        when(bookingClient.getById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok("booking"));

        mockMvc.perform(get("/bookings/10")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk());

        verify(bookingClient).getById(3L, 10L);
    }

    @Test
    void getAllByUser() throws Exception {
        when(bookingClient.getAllByUser(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok("list"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 4L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByUser(4L, "ALL");
    }

    @Test
    void getAllByOwner() throws Exception {
        when(bookingClient.getAllByOwner(anyLong(), anyString()))
                .thenReturn(ResponseEntity.ok("list"));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 5L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByOwner(5L, "WAITING");
    }
}