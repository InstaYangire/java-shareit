package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ItemRequestClientTest {

    private RestTemplate restTemplate;
    private ItemRequestClient requestClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);

        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        requestClient = new ItemRequestClient("http://localhost", builder);
    }

    @Test
    void create_shouldCallPost() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a hammer")
                .build();

        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = requestClient.create(1L, dto);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains(""), eq(HttpMethod.POST), any(), eq(Object.class));
    }

    @Test
    void getOwn_shouldCallGet() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = requestClient.getOwn(2L);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains(""), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void getAll_shouldCallGet() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = requestClient.getAll(3L);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/all"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void getById_shouldCallGet() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = requestClient.getById(4L, 10L);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/10"), eq(HttpMethod.GET), any(), eq(Object.class));
    }
}