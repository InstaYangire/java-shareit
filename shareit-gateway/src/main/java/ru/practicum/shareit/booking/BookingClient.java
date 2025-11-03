package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class BookingClient {
    private final RestTemplate restTemplate;

    @Value("${shareit-server.url}")
    private String serverUrl;

    public ResponseEntity<Object> create(Long userId, BookingDto dto) {
        HttpEntity<Object> request = HttpUtils.makeRequest(dto, userId);
        try {
            return restTemplate.postForEntity(serverUrl + "/bookings", request, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<Object> approve(Long ownerId, Long bookingId, boolean approved) {
        String url = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/bookings/" + bookingId)
                .queryParam("approved", approved)
                .toUriString();

        return restTemplate.exchange(url, HttpMethod.PATCH,
                HttpUtils.makeRequest(null, ownerId), Object.class);
    }

    public ResponseEntity<Object> getById(Long userId, Long bookingId) {
        return restTemplate.exchange(
                serverUrl + "/bookings/" + bookingId,
                HttpMethod.GET,
                HttpUtils.makeRequest(null, userId),
                Object.class);
    }

    public ResponseEntity<Object> getAllByUser(Long userId, String state) {
        String url = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/bookings")
                .queryParam("state", state)
                .toUriString();

        return restTemplate.exchange(url, HttpMethod.GET,
                HttpUtils.makeRequest(null, userId), Object.class);
    }

    public ResponseEntity<Object> getAllByOwner(Long ownerId, String state) {
        String url = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/bookings/owner")
                .queryParam("state", state)
                .toUriString();

        return restTemplate.exchange(url, HttpMethod.GET,
                HttpUtils.makeRequest(null, ownerId),
                Object.class);
    }
}