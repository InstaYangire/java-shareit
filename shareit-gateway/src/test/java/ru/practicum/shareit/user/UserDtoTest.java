package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testSerialize() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .build();

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Alice");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("alice@example.com");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{"
                + "\"id\": 1,"
                + "\"name\": \"Alice\","
                + "\"email\": \"alice@example.com\""
                + "}";

        UserDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Alice");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
    }
}