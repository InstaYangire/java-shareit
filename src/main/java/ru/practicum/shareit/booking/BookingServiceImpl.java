package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingResponseDto create(Long userId, BookingDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book own item");
        }
        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().isEqual(dto.getStart())) {
            throw new ValidationException("Invalid booking dates");
        }

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Only owner can approve booking");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking already processed");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.saveAndFlush(booking);

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getBooker().getId().equals(userId)
                && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("User has no access to this booking");
        }

        return BookingMapper.toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByUser(Long userId, String state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Booking> bookings = bookingRepository.findAllByBooker(user);
        return filterAndSort(bookings, state).stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String state) {
        List<Booking> all = bookingRepository.findAll().stream()
                .filter(b -> b.getItem() != null
                        && b.getItem().getOwner() != null
                        && b.getItem().getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());

        if (all.isEmpty()) {
            throw new ForbiddenException("User is not an owner of any items");
        }

        return filterAndSort(all, state).stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private List<Booking> filterAndSort(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .filter(b -> switch (state.toUpperCase()) {
                    case "ALL" -> true;
                    case "CURRENT" -> !b.getStart().isAfter(now) && !b.getEnd().isBefore(now);
                    case "PAST" -> b.getEnd().isBefore(now);
                    case "FUTURE" -> b.getStart().isAfter(now);
                    case "WAITING" -> b.getStatus() == BookingStatus.WAITING;
                    case "REJECTED" -> b.getStatus() == BookingStatus.REJECTED;
                    default -> true;
                })
                .sorted((b1, b2) -> b2.getStart().compareTo(b1.getStart()))
                .collect(Collectors.toList());
    }
}