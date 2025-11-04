package ru.practicum.shareit.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    private Long id;

    @NotBlank(message = "Item name must not be blank")
    private String name;

    @NotBlank(message = "Item description must not be blank")
    private String description;

    @NotNull(message = "Item availability must be specified")
    private Boolean available;

    private Long requestId;
}