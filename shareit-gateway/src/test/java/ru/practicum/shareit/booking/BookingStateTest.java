package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookingStateTest {

    @Test
    void values_shouldContainAllStates() {
        BookingState[] values = BookingState.values();
        assertEquals(6, values.length);
    }

    @Test
    void valueOf_shouldReturnCorrectEnum() {
        BookingState state = BookingState.valueOf("WAITING");
        assertNotNull(state);
        assertEquals(BookingState.WAITING, state);
    }
}