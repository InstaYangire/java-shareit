package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemRequestDto dto) {
        return requestClient.create(userId, dto);
    }

    @GetMapping()
    public ResponseEntity<Object> getOwn(@RequestHeader("X-Sharer-User-Id") Long userId) {
        ResponseEntity<Object> result = requestClient.getOwn(userId);
        return result;
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.getAll(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable("requestId") Long requestId) {
        ResponseEntity<Object> result = requestClient.getById(userId, requestId);
        return result;
    }
}