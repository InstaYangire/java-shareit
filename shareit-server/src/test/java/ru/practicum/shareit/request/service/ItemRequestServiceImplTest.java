package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository repository;
    @Mock
    private UserService userService;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl service;

    private ItemRequest request;
    private ItemRequestDto requestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        request = ItemRequest.builder()
                .id(1L)
                .description("Need hammer")
                .requesterId(2L)
                .created(LocalDateTime.now())
                .build();

        requestDto = ItemRequestMapper.toDto(request);

        item = Item.builder()
                .id(10L)
                .name("Hammer")
                .description("Steel hammer")
                .available(true)
                .build();
    }

    @Test
    void createRequest_shouldSaveNewRequest() {
        when(userService.getById(anyLong())).thenReturn(null);
        when(repository.save(any(ItemRequest.class))).thenReturn(request);

        ItemRequestDto result = service.createRequest(2L, requestDto);

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        verify(userService).getById(2L);
        verify(repository).save(any(ItemRequest.class));
    }

    @Test
    void getUserRequests_shouldReturnRequestsForUser() {
        when(userService.getById(anyLong())).thenReturn(null);
        when(repository.findByRequesterIdOrderByCreatedDesc(2L))
                .thenReturn(List.of(request));

        List<ItemRequestDto> result = service.getUserRequests(2L);

        assertEquals(1, result.size());
        assertEquals("Need hammer", result.get(0).getDescription());
        verify(userService).getById(2L);
    }

    @Test
    void getUserRequests_shouldReturnEmptyListWhenNoRequests() {
        when(userService.getById(anyLong())).thenReturn(null);
        when(repository.findByRequesterIdOrderByCreatedDesc(2L))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = service.getUserRequests(2L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getOtherUsersRequests_shouldReturnList() {
        when(userService.getById(anyLong())).thenReturn(null);
        when(repository.findByRequesterIdNotOrderByCreatedDesc(1L))
                .thenReturn(List.of(request));

        List<ItemRequestDto> result = service.getOtherUsersRequests(1L);

        assertEquals(1, result.size());
        assertEquals(request.getDescription(), result.get(0).getDescription());
        verify(repository).findByRequesterIdNotOrderByCreatedDesc(1L);
    }

    @Test
    void getOtherUsersRequests_shouldReturnEmptyListWhenNoRequests() {
        when(userService.getById(anyLong())).thenReturn(null);
        when(repository.findByRequesterIdNotOrderByCreatedDesc(1L))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = service.getOtherUsersRequests(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        when(userService.getById(anyLong())).thenReturn(null);
        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequest_Id(1L)).thenReturn(List.of(item));

        ItemRequestResponseDto result = service.getRequestById(2L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need hammer", result.getDescription());
        assertEquals(1, result.getItems().size());
        assertEquals("Hammer", result.getItems().get(0).getName());
        verify(itemRepository).findByRequest_Id(1L);
    }

    @Test
    void getRequestById_shouldThrowWhenNotFound() {
        when(userService.getById(anyLong())).thenReturn(null);
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getRequestById(1L, 99L));
        verify(repository).findById(99L);
    }

    @Test
    void getRequestById_shouldHandleEmptyItemsList() {
        when(userService.getById(anyLong())).thenReturn(null);
        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequest_Id(anyLong())).thenReturn(Collections.emptyList());

        ItemRequestResponseDto result = service.getRequestById(2L, 1L);

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void createRequest_shouldThrowIfUserNotFound() {
        doThrow(new NotFoundException("User not found")).when(userService).getById(5L);
        assertThrows(NotFoundException.class, () -> service.createRequest(5L, requestDto));
    }
}