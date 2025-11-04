package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.ForbiddenException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl service;

    private User owner;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setup() {
        owner = new User(1L, "Alice", "alice@mail.com");
        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Cordless drill")
                .available(true)
                .owner(owner)
                .build();

        itemDto = ItemMapper.toItemDto(item);
    }

    @Test
    void shouldCreateItemWithoutRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto result = service.create(1L, itemDto);

        assertEquals(itemDto.getName(), result.getName());
        verify(itemRepository, times(1)).save(any());
    }

    @Test
    void shouldCreateItemWithRequestId() {
        ItemRequest request = new ItemRequest(10L, "Need hammer", 2L, LocalDateTime.now());
        itemDto.setRequestId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto result = service.create(1L, itemDto);

        assertNotNull(result);
        verify(itemRequestRepository).findById(10L);
    }

    @Test
    void shouldThrowWhenOwnerNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(1L, itemDto));
    }

    @Test
    void shouldThrowWhenRequestNotFoundById() {
        itemDto.setRequestId(99L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(1L, itemDto));
    }

    @Test
    void shouldUpdateItem() {
        ItemDto updateDto = ItemDto.builder().name("Updated Drill").available(false).build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto result = service.update(1L, 1L, updateDto);

        assertEquals("Updated Drill", result.getName());
        verify(itemRepository).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingNotOwnerItem() {
        User another = new User(2L, "Bob", "b@mail.com");
        item.setOwner(another);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () -> service.update(1L, 1L, itemDto));
    }

    @Test
    void shouldNotChangeAnythingWhenUpdateFieldsNull() {
        ItemDto updateDto = new ItemDto();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenReturn(item);

        ItemDto result = service.update(1L, 1L, updateDto);

        assertEquals(item.getName(), result.getName());
        verify(itemRepository).save(item);
    }

    @Test
    void shouldReturnItemWithComments() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItem(item)).thenReturn(List.of(
                Comment.builder().id(1L).text("Nice!").author(owner).item(item).created(LocalDateTime.now()).build()
        ));

        var result = service.getById(1L, 1L);

        assertEquals("Drill", result.getName());
        assertEquals(1, result.getComments().size());
    }

    @Test
    void shouldThrowWhenItemNotFoundById() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(99L, 1L));
    }

    @Test
    void shouldNotSetBookingsIfNotOwner() {
        User another = new User(2L, "Bob", "b@mail.com");
        item.setOwner(another);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItem(item)).thenReturn(List.of());

        var result = service.getById(1L, 1L);

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void shouldReturnItemsByOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwner(owner)).thenReturn(List.of(item));
        when(commentRepository.findAllByItem(any())).thenReturn(List.of());

        var result = service.getByOwner(1L);

        assertEquals(1, result.size());
        verify(itemRepository).findAllByOwner(owner);
    }

    @Test
    void shouldThrowWhenOwnerNotExistsInGetByOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getByOwner(1L));
    }

    @Test
    void shouldHandleOwnerItemsWithoutCommentsOrBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwner(owner)).thenReturn(List.of(item));
        when(commentRepository.findAllByItem(any())).thenReturn(List.of());
        when(bookingRepository.findAllByItem(any())).thenReturn(List.of());

        var result = service.getByOwner(1L);

        assertEquals(1, result.size());
        assertNull(result.get(0).getLastBooking());
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextBlank() {
        var result = service.search("  ");
        assertTrue(result.isEmpty());
        verifyNoInteractions(itemRepository);
    }

    @Test
    void shouldReturnEmptyListWhenSearchTextIsNull() {
        var result = service.search(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSearchItems() {
        when(itemRepository.search("drill")).thenReturn(List.of(item));
        var result = service.search("drill");
        assertEquals(1, result.size());
        verify(itemRepository).search("drill");
    }

    @Test
    void shouldAddCommentWhenBookingExists() {
        CommentDto commentDto = new CommentDto(null, "Good item", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                anyLong(), anyLong(), eq(BookingStatus.APPROVED), any())
        ).thenReturn(true);
        Comment comment = Comment.builder().id(1L).text("Good item").author(owner).item(item).build();
        when(commentRepository.save(any())).thenReturn(comment);

        CommentDto result = service.addComment(1L, 1L, commentDto);

        assertEquals("Good item", result.getText());
        verify(commentRepository).save(any());
    }

    @Test
    void shouldThrowWhenUserHasNoCompletedBooking() {
        CommentDto commentDto = new CommentDto(null, "Bad", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(any(), any(), any(), any()))
                .thenReturn(false);
        assertThrows(ValidationException.class, () -> service.addComment(1L, 1L, commentDto));
    }

    @Test
    void shouldThrowWhenCommentUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.addComment(1L, 1L, new CommentDto()));
    }

    @Test
    void shouldThrowWhenItemNotFoundForComment() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.addComment(1L, 1L, new CommentDto()));
    }

    @Test
    void shouldCheckBookingExistenceWhenAddingComment() {
        CommentDto commentDto = new CommentDto(null, "Nice", null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(), any())
        ).thenReturn(true);
        when(commentRepository.save(any()))
                .thenReturn(Comment.builder().id(1L).text("Nice").item(item).author(owner).build());

        service.addComment(1L, 1L, commentDto);

        verify(bookingRepository).existsByBookerIdAndItemIdAndStatusAndEndBefore(
                anyLong(), anyLong(), any(), any(LocalDateTime.class));
    }
}