package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BaseClientTest {

    private RestTemplate restTemplate;
    private BaseClient baseClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        baseClient = new BaseClient(restTemplate);
    }

    @Test
    void get_shouldCallExchangeAndReturnResponse() {
        ResponseEntity<Object> expected = ResponseEntity.ok("ok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> result = baseClient.get("/path");

        assertEquals(200, result.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/path"), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void post_shouldReturnOk() {
        ResponseEntity<Object> expected = ResponseEntity.ok("done");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> result = baseClient.post("/post", 1L, "data");

        assertEquals(200, result.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/post"), eq(HttpMethod.POST), any(), eq(Object.class));
    }

    @Test
    void patch_shouldReturnOk() {
        ResponseEntity<Object> expected = ResponseEntity.ok("patched");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PATCH), any(), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> result = baseClient.patch("/edit", 2L, "patchData");

        assertEquals(200, result.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/edit"), eq(HttpMethod.PATCH), any(), eq(Object.class));
    }

    @Test
    void delete_shouldReturnOk() {
        ResponseEntity<Object> expected = ResponseEntity.ok().build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> result = baseClient.delete("/delete", 3L);

        assertEquals(200, result.getStatusCodeValue());
        verify(restTemplate).exchange(contains("/delete"), eq(HttpMethod.DELETE), any(), eq(Object.class));
    }

    @Test
    void shouldHandleHttpStatusCodeException() {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getResponseBodyAsString()).thenReturn("Bad request");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = baseClient.get("/error");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Bad request", response.getBody());
    }

    @Test
    void defaultHeaders_shouldNotSetUserIdWhenNull() {
        ResponseEntity<Object> expected = ResponseEntity.ok("ok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expected);

        ResponseEntity<Object> result = baseClient.get("/path", null, null);

        assertEquals(200, result.getStatusCodeValue());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class));
    }

    @Test
    void prepareGatewayResponse_shouldReturnWithoutBody() {
        ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        ResponseEntity<Object> result = invokePrepareGatewayResponse(response);

        assertEquals(204, result.getStatusCodeValue());
        assertEquals(null, result.getBody());
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> invokePrepareGatewayResponse(ResponseEntity<Object> response) {
        try {
            var method = BaseClient.class.getDeclaredMethod("prepareGatewayResponse", ResponseEntity.class);
            method.setAccessible(true);
            return (ResponseEntity<Object>) method.invoke(null, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void get_shouldCallExchangeWithParameters() {
        ResponseEntity<Object> expected = ResponseEntity.ok("ok");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class), anyMap()))
                .thenReturn(expected);

        Map<String, Object> params = Map.of("key", "value");

        ResponseEntity<Object> result = baseClient.get("/path", 1L, params);

        assertEquals(200, result.getStatusCodeValue());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class), eq(params));
    }

    @Test
    void prepareGatewayResponse_shouldReturnWithBodyForErrorStatus() {
        ResponseEntity<Object> response = ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("error");

        ResponseEntity<Object> result = invokePrepareGatewayResponse(response);

        assertEquals(400, result.getStatusCodeValue());
        assertEquals("error", result.getBody());
    }
}