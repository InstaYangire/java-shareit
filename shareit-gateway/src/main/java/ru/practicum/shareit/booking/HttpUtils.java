package ru.practicum.shareit.booking;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class HttpUtils {
    public static HttpEntity<Object> makeRequest(Object body, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Sharer-User-Id", String.valueOf(userId));
        return new HttpEntity<>(body, headers);
    }
}