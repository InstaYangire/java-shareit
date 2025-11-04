package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.shareit.client.BaseClient;

@Service
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";
    private final String serverUrl;

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder
                .uriTemplateHandler(new org.springframework.web.util.DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                .build());
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Object> create(Long userId, BookingDto dto) {
        HttpEntity<Object> request = HttpUtils.makeRequest(dto, userId);
        try {
            return rest.postForEntity(serverUrl + API_PREFIX, request, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<Object> approve(Long ownerId, Long bookingId, boolean approved) {
        String url = UriComponentsBuilder
                .fromHttpUrl(serverUrl + API_PREFIX + "/" + bookingId)
                .queryParam("approved", approved)
                .toUriString();

        return rest.exchange(url, HttpMethod.PATCH, HttpUtils.makeRequest(null, ownerId), Object.class);
    }

    public ResponseEntity<Object> getById(Long userId, Long bookingId) {
        return rest.exchange(
                serverUrl + API_PREFIX + "/" + bookingId,
                HttpMethod.GET,
                HttpUtils.makeRequest(null, userId),
                Object.class);
    }

    public ResponseEntity<Object> getAllByUser(Long userId, String state) {
        String url = UriComponentsBuilder
                .fromHttpUrl(serverUrl + API_PREFIX)
                .queryParam("state", state)
                .toUriString();

        return rest.exchange(url, HttpMethod.GET, HttpUtils.makeRequest(null, userId), Object.class);
    }

    public ResponseEntity<Object> getAllByOwner(Long ownerId, String state) {
        String url = UriComponentsBuilder
                .fromHttpUrl(serverUrl + API_PREFIX + "/owner")
                .queryParam("state", state)
                .toUriString();

        return rest.exchange(url, HttpMethod.GET, HttpUtils.makeRequest(null, ownerId), Object.class);
    }
}