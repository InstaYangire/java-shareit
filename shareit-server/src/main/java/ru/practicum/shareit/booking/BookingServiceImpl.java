package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
            throw new ValidationException("Owner cannot book own item");
        }
        if (dto.getStart() == null || dto.getEnd() == null
                || !dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("Invalid booking dates");
        }

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

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
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        Long ownerId = booking.getItem().getOwner().getId();
        Long bookerId = booking.getBooker().getId();
        if (!bookerId.equals(userId) && !ownerId.equals(userId)) {
            throw new NotFoundException("User has no access to this booking");
        }
        return BookingMapper.toResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByUser(Long userId, String state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        List<Booking> bookings = bookingRepository.findAllByBookerOrderByStartDesc(user);
        return filter(bookings, state).stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByOwner(Long ownerId, String state) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("User not found");
        }
        List<Booking> bookings = bookingRepository.findAllByItem_Owner_IdOrderByStartDesc(ownerId);
        return filter(bookings, state).stream()
                .map(BookingMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private List<Booking> filter(List<Booking> bookings, String stateRaw) {
        String state = stateRaw == null ? "ALL" : stateRaw.toUpperCase();
        LocalDateTime now = LocalDateTime.now();

        return bookings.stream().filter(b -> switch (state) {
            case "ALL" -> true;
            case "CURRENT" -> !b.getStart().isAfter(now) && !b.getEnd().isBefore(now);
            case "PAST" -> b.getEnd().isBefore(now);
            case "FUTURE" -> b.getStart().isAfter(now);
            case "WAITING" -> b.getStatus() == BookingStatus.WAITING;
            case "REJECTED" -> b.getStatus() == BookingStatus.REJECTED;
            default -> throw new ValidationException("Unknown state: " + stateRaw);
        }).collect(Collectors.toList());
    }
}