package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.client.BaseClient;

@Service
public class ItemClient extends BaseClient {

    public ItemClient(
            @Value("${shareit-server.url}") String serverUrl,
            RestTemplateBuilder builder
    ) {
        super(builder
                .uriTemplateHandler(
                        new org.springframework.web.util.DefaultUriBuilderFactory(serverUrl + "/items")
                )
                .build()
        );
    }

    public ResponseEntity<Object> create(Long ownerId, ItemDto dto) {
        return post("", ownerId, dto);
    }

    public ResponseEntity<Object> update(Long itemId, Long ownerId, ItemDto dto) {
        return patch("/" + itemId, ownerId, dto);
    }

    public ResponseEntity<Object> getById(Long itemId, Long userId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getByOwner(Long ownerId) {
        return get("", ownerId);
    }

    public ResponseEntity<Object> search(String text) {
        return get("/search?text=" + text);
    }

    public ResponseEntity<Object> addComment(Long userId, Long itemId, CommentDto commentDto) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }
}