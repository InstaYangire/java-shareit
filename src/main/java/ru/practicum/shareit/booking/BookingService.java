package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto create(Long userId, BookingDto bookingDto);

    BookingResponseDto approve(Long ownerId, Long bookingId, boolean approved);

    BookingResponseDto getById(Long userId, Long bookingId);

    List<BookingResponseDto> getAllByUser(Long userId, String state);

    List<BookingResponseDto> getAllByOwner(Long ownerId, String state);
}