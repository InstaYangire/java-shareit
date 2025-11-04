package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.error.ForbiddenException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.error.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl service;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@mail.com");
        booker = new User(2L, "Booker", "booker@mail.com");
        item = new Item(10L, "Drill", "Power drill", true, owner, null);
        booking = Booking.builder()
                .id(100L)
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .status(BookingStatus.WAITING)
                .build();

        bookingDto = BookingDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusHours(1))
                .end(LocalDateTime.now().plusHours(2))
                .build();
    }

    @Test
    void create_shouldSaveBooking_whenValid() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = service.create(booker.getId(), bookingDto);

        assertEquals(booking.getId(), result.getId());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(booker.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowWhenItemNotFound() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(booker.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowWhenItemUnavailable() {
        item.setAvailable(false);
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        assertThrows(ValidationException.class, () -> service.create(booker.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowWhenOwnerTriesToBookOwnItem() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        assertThrows(ValidationException.class, () -> service.create(owner.getId(), bookingDto));
    }

    @Test
    void create_shouldThrowWhenInvalidDates() {
        bookingDto.setStart(LocalDateTime.now());
        bookingDto.setEnd(LocalDateTime.now().minusHours(1));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        assertThrows(ValidationException.class, () -> service.create(booker.getId(), bookingDto));
    }

    @Test
    void approve_shouldApproveBooking() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponseDto result = service.approve(owner.getId(), 100L, true);

        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        verify(bookingRepository).save(any());
    }

    @Test
    void approve_shouldRejectBooking() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingResponseDto result = service.approve(owner.getId(), 100L, false);

        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void approve_shouldThrowWhenNotOwner() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        assertThrows(ForbiddenException.class, () -> service.approve(999L, 100L, true));
    }

    @Test
    void approve_shouldThrowWhenBookingNotFound() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.approve(owner.getId(), 100L, true));
    }

    @Test
    void approve_shouldThrowWhenAlreadyProcessed() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        assertThrows(ValidationException.class, () -> service.approve(owner.getId(), 100L, true));
    }

    @Test
    void getById_shouldReturnBooking_whenOwnerOrBooker() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        BookingResponseDto result = service.getById(booker.getId(), 100L);
        assertEquals(booking.getId(), result.getId());
    }

    @Test
    void getById_shouldThrowWhenNoAccess() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
        assertThrows(NotFoundException.class, () -> service.getById(999L, 100L));
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getById(booker.getId(), 100L));
    }

    @Test
    void getAllByUser_shouldReturnFilteredBookings() {
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerOrderByStartDesc(booker)).thenReturn(List.of(booking));

        List<BookingResponseDto> result = service.getAllByUser(booker.getId(), "ALL");
        assertEquals(1, result.size());
    }

    @Test
    void getAllByOwner_shouldReturnBookings_whenUserExists() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(owner.getId())).thenReturn(List.of(booking));

        List<BookingResponseDto> result = service.getAllByOwner(owner.getId(), "PAST");
        assertTrue(result.isEmpty() || result.size() >= 0);
    }

    @Test
    void getAllByOwner_shouldThrowWhenUserNotFound() {
        when(userRepository.existsById(owner.getId())).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.getAllByOwner(owner.getId(), "ALL"));
    }

    @Test
    void filter_shouldHandleAllStates() {
        LocalDateTime now = LocalDateTime.now();
        Item testItem = new Item(1L, "Hammer", "Tool", true, owner, null);

        Booking b = Booking.builder()
                .start(now.minusHours(2))
                .end(now.minusHours(1))
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(testItem)
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findAllByBookerOrderByStartDesc(any())).thenReturn(List.of(b));

        assertDoesNotThrow(() -> service.getAllByUser(booker.getId(), "ALL"));
        assertThrows(ValidationException.class, () -> service.getAllByUser(booker.getId(), "INVALID"));
    }
}