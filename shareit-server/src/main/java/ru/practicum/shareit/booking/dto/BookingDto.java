package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {
    private Long id;

    @NotNull(message = "Start time cannot be null")
    private LocalDateTime start;

    @NotNull(message = "End time cannot be null")
    private LocalDateTime end;

    @NotNull(message = "Item ID cannot be null")
    private Long itemId;

    private Long bookerId;
    private BookingStatus status;
}