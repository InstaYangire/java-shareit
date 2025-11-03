package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private BookingController bookingController;

    private MockMvc mockMvc;
    private ObjectMapper mapper;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(2L)
                .build();
    }

    @Test
    void create_shouldReturnOk() throws Exception {
        when(bookingClient.create(anyLong(), any())).thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());
    }

    @Test
    void approve_shouldReturnOk() throws Exception {
        when(bookingClient.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(bookingDto));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturnOk() throws Exception {
        when(bookingClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok(bookingDto));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByUser_shouldReturnOk() throws Exception {
        when(bookingClient.getAllByUser(anyLong(), anyString())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 4L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwner_shouldReturnOk() throws Exception {
        when(bookingClient.getAllByOwner(anyLong(), anyString())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 5L)
                        .param("state", "WAITING"))
                .andExpect(status().isOk());
    }
}