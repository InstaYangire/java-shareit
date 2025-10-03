package ru.practicum.shareit.request;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    private Long id;
    private String description;
    private Long requesterId;     // User's id
    private LocalDateTime created;
}