package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserClientTest {

    private RestTemplate restTemplate;
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);

        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(java.util.function.Supplier.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        userClient = new UserClient("http://localhost", builder);
    }

    @Test
    void getAllUsers_shouldCallGet() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.getAllUsers();

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains(""), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void getUserById_shouldCallGet() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.getUserById(5L);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/5"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void createUser_shouldCallPost() {
        UserDto dto = UserDto.builder()
                .name("Alice")
                .email("alice@example.com")
                .build();

        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.createUser(dto);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains(""), eq(HttpMethod.POST), any(), eq(Object.class));
    }

    @Test
    void updateUser_shouldCallPatch() {
        UserDto dto = UserDto.builder()
                .name("Bob")
                .email("bob@example.com")
                .build();

        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.updateUser(10L, dto);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/10"), eq(HttpMethod.PATCH), any(), eq(Object.class));
    }

    @Test
    void deleteUser_shouldCallDelete() {
        when(restTemplate.exchange(anyString(), any(), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<Object> response = userClient.deleteUser(7L);

        assertEquals(200, response.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/7"), eq(HttpMethod.DELETE), any(), eq(Object.class));
    }
}