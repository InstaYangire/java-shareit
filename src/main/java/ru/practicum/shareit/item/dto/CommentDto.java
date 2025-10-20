package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {

    private Long id;

    @NotBlank(message = "Comment text must not be blank")
    private String text;

    private String authorName;

    private LocalDateTime created;
}