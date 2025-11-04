package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

public class ItemRequestMapper {

    public static ItemRequest toModel(ItemRequestDto dto, Long userId) {
        return ItemRequest.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .requesterId(userId)
                .created(LocalDateTime.now())
                .build();
    }

    public static ItemRequestDto toDto(ItemRequest entity) {
        return ItemRequestDto.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .requesterId(entity.getRequesterId())
                .created(entity.getCreated())
                .build();
    }
}