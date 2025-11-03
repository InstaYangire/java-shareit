package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ItemClientTest {

    private RestTemplate restTemplate;
    private ItemClient itemClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);

        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        itemClient = new ItemClient("http://localhost", builder);
    }

    @Test
    void create_shouldCallPost() {
        ItemDto dto = ItemDto.builder()
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .build();

        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemClient.create(1L, dto);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains(""), eq(HttpMethod.POST), any(), eq(Object.class));
    }

    @Test
    void update_shouldCallPatch() {
        ItemDto dto = ItemDto.builder()
                .name("Updated")
                .description("Updated desc")
                .available(false)
                .build();

        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemClient.update(2L, 1L, dto);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/2"), eq(HttpMethod.PATCH), any(), eq(Object.class));
    }

    @Test
    void getById_shouldCallGet() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemClient.getById(10L, 5L);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/10"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void getByOwner_shouldCallGet() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemClient.getByOwner(3L);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains(""), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void search_shouldCallGet() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemClient.search("hammer");

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/search?text=hammer"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void addComment_shouldCallPost() {
        CommentDto comment = CommentDto.builder()
                .text("Nice tool!")
                .build();

        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = itemClient.addComment(3L, 2L, comment);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/2/comment"), eq(HttpMethod.POST), any(), eq(Object.class));
    }
}