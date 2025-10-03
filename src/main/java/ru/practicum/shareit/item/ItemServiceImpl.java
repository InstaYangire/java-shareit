package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.ForbiddenException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> items = new HashMap<>();
    private long idCounter = 1;

    private final UserService userService;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        validateItem(itemDto);

        if (!userService.existsById(ownerId)) {
            throw new NotFoundException("Owner not found with id: " + ownerId);
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setId(idCounter++);
        item.setOwnerId(ownerId);
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(Long itemId, Long ownerId, ItemDto itemDto) {
        Item existing = items.get(itemId);
        if (existing == null) {
            throw new NotFoundException("Item with id " + itemId + " not found");
        }
        if (!Objects.equals(existing.getOwnerId(), ownerId)) {
            throw new ForbiddenException("Only owner can update item");
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(existing);
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = items.get(id);
        if (item == null) {
            throw new NotFoundException("Item with id " + id + " not found");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> Objects.equals(item.getOwnerId(), ownerId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lower = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        (item.getName() != null && item.getName().toLowerCase().contains(lower)) ||
                                (item.getDescription() != null && item.getDescription().toLowerCase().contains(lower))
                )
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item name must not be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description must not be empty");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Item availability must be specified");
        }
    }
}