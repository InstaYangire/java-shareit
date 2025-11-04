package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemMapperTest {

    @Test
    void toItemDto_shouldMapCorrectly() {
        User owner = User.builder().id(1L).name("John").email("j@j.com").build();
        ItemRequest request = ItemRequest.builder().id(9L).build();
        Item item = Item.builder()
                .id(2L)
                .name("Hammer")
                .description("Steel hammer")
                .available(true)
                .owner(owner)
                .request(request)
                .build();

        ItemDto dto = ItemMapper.toItemDto(item);

        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getName(), dto.getName());
        assertEquals(item.getDescription(), dto.getDescription());
        assertEquals(item.getAvailable(), dto.getAvailable());
        assertEquals(owner.getId(), dto.getOwnerId());
        assertEquals(9L, dto.getRequestId());
    }

    @Test
    void toItemDto_shouldReturnNullWhenItemIsNull() {
        assertNull(ItemMapper.toItemDto(null));
    }

    @Test
    void toItemDtoWithComments_shouldIncludeComments() {
        User owner = User.builder().id(1L).build();
        Item item = Item.builder()
                .id(3L)
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .owner(owner)
                .build();

        CommentDto comment = CommentDto.builder()
                .id(5L)
                .text("Nice one!")
                .authorName("User1")
                .build();

        ItemDto dto = ItemMapper.toItemDtoWithComments(item, List.of(comment));

        assertEquals(item.getId(), dto.getId());
        assertEquals("Drill", dto.getName());
        assertEquals(1, dto.getComments().size());
        assertEquals("Nice one!", dto.getComments().get(0).getText());
    }

    @Test
    void toItemDtoWithComments_shouldReturnNullWhenItemIsNull() {
        assertNull(ItemMapper.toItemDtoWithComments(null, List.of()));
    }

    @Test
    void toItem_shouldMapDtoToEntityWithoutRequest() {
        User owner = User.builder().id(1L).name("User").build();
        ItemDto dto = ItemDto.builder()
                .id(10L)
                .name("Saw")
                .description("Wood saw")
                .available(true)
                .build();

        Item item = ItemMapper.toItem(dto, owner);

        assertEquals(dto.getId(), item.getId());
        assertEquals("Saw", item.getName());
        assertEquals("Wood saw", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(owner, item.getOwner());
        assertNull(item.getRequest());
    }

    @Test
    void toItem_shouldMapWithRequestId() {
        User owner = User.builder().id(1L).build();
        ItemDto dto = ItemDto.builder()
                .id(20L)
                .name("Wrench")
                .description("Adjustable wrench")
                .available(true)
                .requestId(7L)
                .build();

        Item item = ItemMapper.toItem(dto, owner);

        assertNotNull(item.getRequest());
        assertEquals(7L, item.getRequest().getId());
    }

    @Test
    void toItem_shouldReturnNullWhenDtoIsNull() {
        assertNull(ItemMapper.toItem(null, new User()));
    }

    @Test
    void toItemShortDto_shouldMapCorrectly() {
        ItemRequest request = ItemRequest.builder().id(5L).build();
        Item item = Item.builder()
                .id(2L)
                .name("Lamp")
                .description("Desk lamp")
                .available(false)
                .request(request)
                .build();

        ItemShortDto dto = ItemMapper.toItemShortDto(item);

        assertEquals(item.getId(), dto.getId());
        assertEquals("Lamp", dto.getName());
        assertEquals("Desk lamp", dto.getDescription());
        assertFalse(dto.getAvailable());
        assertEquals(5L, dto.getRequestId());
    }

    @Test
    void toItemShortDto_shouldReturnNullWhenItemIsNull() {
        assertNull(ItemMapper.toItemShortDto(null));
    }
}