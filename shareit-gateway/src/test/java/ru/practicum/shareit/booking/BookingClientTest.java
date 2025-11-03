package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingClientTest {

    private RestTemplate restTemplate;
    private BookingClient bookingClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);
        bookingClient = new BookingClient("http://localhost", builder);
    }

    @Test
    void create_success() {
        BookingDto dto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        HttpEntity<Object> request = new HttpEntity<>(dto);
        ResponseEntity<Object> expected = ResponseEntity.ok("ok");

        try (MockedStatic<HttpUtils> mocked = mockStatic(HttpUtils.class)) {
            mocked.when(() -> HttpUtils.makeRequest(dto, 1L)).thenReturn(request);
            when(restTemplate.postForEntity(anyString(), eq(request), eq(Object.class)))
                    .thenReturn(expected);

            ResponseEntity<Object> result = bookingClient.create(1L, dto);

            assertEquals(200, result.getStatusCodeValue());
            assertEquals("ok", result.getBody());
            verify(restTemplate).postForEntity(contains("/bookings"), eq(request), eq(Object.class));
        }
    }

    @Test
    void create_handlesHttpStatusCodeException() {
        BookingDto dto = BookingDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        HttpEntity<Object> request = new HttpEntity<>(dto);
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsString()).thenReturn("error");

        try (MockedStatic<HttpUtils> mocked = mockStatic(HttpUtils.class)) {
            mocked.when(() -> HttpUtils.makeRequest(dto, 2L)).thenReturn(request);
            when(restTemplate.postForEntity(anyString(), eq(request), eq(Object.class)))
                    .thenThrow(exception);

            ResponseEntity<Object> result = bookingClient.create(2L, dto);

            assertEquals(400, result.getStatusCodeValue());
            assertEquals("error", result.getBody());
        }
    }

    @Test
    void approve_success() {
        ResponseEntity<Object> expected = ResponseEntity.ok("patched");
        try (MockedStatic<HttpUtils> mocked = mockStatic(HttpUtils.class)) {
            mocked.when(() -> HttpUtils.makeRequest(null, 3L)).thenReturn(new HttpEntity<>(null));
            when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(), eq(Object.class)))
                    .thenReturn(expected);

            ResponseEntity<Object> result = bookingClient.approve(3L, 5L, true);

            assertEquals(200, result.getStatusCodeValue());
            assertEquals("patched", result.getBody());
            verify(restTemplate).exchange(contains("/bookings/5"), eq(HttpMethod.PATCH), any(), eq(Object.class));
        }
    }

    @Test
    void getById_success() {
        ResponseEntity<Object> expected = ResponseEntity.ok("one");
        try (MockedStatic<HttpUtils> mocked = mockStatic(HttpUtils.class)) {
            mocked.when(() -> HttpUtils.makeRequest(null, 4L)).thenReturn(new HttpEntity<>(null));
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                    .thenReturn(expected);

            ResponseEntity<Object> result = bookingClient.getById(4L, 10L);

            assertEquals(200, result.getStatusCodeValue());
            assertEquals("one", result.getBody());
            verify(restTemplate).exchange(contains("/bookings/10"), eq(HttpMethod.GET), any(), eq(Object.class));
        }
    }

    @Test
    void getAllByUser_success() {
        ResponseEntity<Object> expected = ResponseEntity.ok("list");
        try (MockedStatic<HttpUtils> mocked = mockStatic(HttpUtils.class)) {
            mocked.when(() -> HttpUtils.makeRequest(null, 6L)).thenReturn(new HttpEntity<>(null));
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                    .thenReturn(expected);

            ResponseEntity<Object> result = bookingClient.getAllByUser(6L, "ALL");

            assertEquals(200, result.getStatusCodeValue());
            assertEquals("list", result.getBody());
            verify(restTemplate).exchange(contains("/bookings?state=ALL"), eq(HttpMethod.GET), any(), eq(Object.class));
        }
    }

    @Test
    void getAllByOwner_success() {
        ResponseEntity<Object> expected = ResponseEntity.ok("ownerList");
        try (MockedStatic<HttpUtils> mocked = mockStatic(HttpUtils.class)) {
            mocked.when(() -> HttpUtils.makeRequest(null, 7L)).thenReturn(new HttpEntity<>(null));
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                    .thenReturn(expected);

            ResponseEntity<Object> result = bookingClient.getAllByOwner(7L, "WAITING");

            assertEquals(200, result.getStatusCodeValue());
            assertEquals("ownerList", result.getBody());
            verify(restTemplate).exchange(contains("/bookings/owner?state=WAITING"), eq(HttpMethod.GET), any(), eq(Object.class));
        }
    }
}