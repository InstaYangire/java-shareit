package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository repository;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto createRequest(Long userId, ItemRequestDto dto) {
        userService.getById(userId);
        ItemRequest newRequest = ItemRequestMapper.toModel(dto, userId);
        return ItemRequestMapper.toDto(repository.save(newRequest));
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        userService.getById(userId);
        return repository.findByRequesterIdOrderByCreatedDesc(userId)
                .stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getOtherUsersRequests(Long userId) {
        userService.getById(userId);
        return repository.findByRequesterIdNotOrderByCreatedDesc(userId)
                .stream()
                .map(ItemRequestMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        userService.getById(userId);

        ItemRequest req = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));

        List<ItemShortDto> items = itemRepository.findByRequest_Id(req.getId())
                .stream()
                .map(ItemMapper::toItemShortDto)
                .collect(Collectors.toList());

        return ItemRequestResponseDto.builder()
                .id(req.getId())
                .description(req.getDescription())
                .created(req.getCreated())
                .items(items)
                .build();
    }
}