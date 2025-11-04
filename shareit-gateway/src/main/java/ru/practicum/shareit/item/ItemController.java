package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @Valid @RequestBody ItemDto itemDto
    ) {
        return itemClient.create(ownerId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @PathVariable Long id,
            @RequestBody ItemDto itemDto
    ) {
        return itemClient.update(id, ownerId, itemDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable Long id,
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        return itemClient.getById(id, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId
    ) {
        return itemClient.getByOwner(ownerId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam String text
    ) {
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto
    ) {
        return itemClient.addComment(userId, itemId, commentDto);
    }
}