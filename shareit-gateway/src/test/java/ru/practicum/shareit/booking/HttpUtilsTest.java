package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;

class HttpUtilsTest {

    @Test
    void makeRequest_shouldCreateEntityWithHeaderAndBody() {
        Long userId = 42L;
        String body = "testBody";

        HttpEntity<Object> result = HttpUtils.makeRequest(body, userId);

        assertNotNull(result, "HttpEntity should not be null");
        assertEquals(body, result.getBody(), "Body should match input object");

        HttpHeaders headers = result.getHeaders();
        assertTrue(headers.containsKey("X-Sharer-User-Id"), "Header X-Sharer-User-Id should be present");
        assertEquals(String.valueOf(userId), headers.getFirst("X-Sharer-User-Id"), "Header should contain correct userId");
    }
}