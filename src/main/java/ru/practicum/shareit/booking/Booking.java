package ru.practicum.shareit.booking;

import lombok.*;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    private Long id;
    private Long itemId;
    private Long bookerId;     // User's id
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
}