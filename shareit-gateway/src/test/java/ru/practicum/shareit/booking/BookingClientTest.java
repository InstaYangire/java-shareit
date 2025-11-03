package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BookingClient bookingClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        bookingClient = new BookingClient(restTemplate);
        ReflectionTestUtils.setField(bookingClient, "serverUrl", "http://localhost:9090");
    }

    @Test
    void create_shouldCallPostAndReturnOk() {
        BookingDto dto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(2L)
                .build();

        ResponseEntity<Object> expected = ResponseEntity.ok().build();

        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class))).thenReturn(expected);

        ResponseEntity<Object> response = bookingClient.create(1L, dto);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).postForEntity(contains("/bookings"), any(), eq(Object.class));
    }

    @Test
    void create_shouldHandleHttpStatusCodeException() {
        BookingDto dto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(2L)
                .build();

        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsString()).thenReturn("Error message");

        when(restTemplate.postForEntity(anyString(), any(), eq(Object.class))).thenThrow(exception);

        ResponseEntity<Object> response = bookingClient.create(1L, dto);

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Error message", response.getBody());
    }

    @Test
    void approve_shouldCallPatch() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = bookingClient.approve(2L, 5L, true);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/bookings/5"), eq(HttpMethod.PATCH), any(), eq(Object.class));
    }

    @Test
    void getById_shouldCallGet() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = bookingClient.getById(3L, 6L);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/bookings/6"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void getAllByUser_shouldCallGet() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = bookingClient.getAllByUser(4L, "ALL");

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/bookings?state=ALL"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void getAllByOwner_shouldCallGet() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = bookingClient.getAllByOwner(5L, "WAITING");

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/bookings/owner?state=WAITING"), eq(HttpMethod.GET), any(), eq(Object.class));
    }
}