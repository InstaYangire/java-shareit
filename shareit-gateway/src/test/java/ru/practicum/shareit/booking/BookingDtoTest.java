package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testSerialize() throws Exception {
        BookingDto dto = BookingDto.builder()
                .start(LocalDateTime.of(2025, 1, 1, 12, 0))
                .end(LocalDateTime.of(2025, 1, 1, 13, 0))
                .itemId(10L)
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2025-01-01T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2025-01-01T13:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{"
                + "\"start\": \"2025-01-01T12:00:00\","
                + "\"end\": \"2025-01-01T13:00:00\","
                + "\"itemId\": 10"
                + "}";

        BookingDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(10L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 1, 13, 0));
    }
}