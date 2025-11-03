package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.error.ConflictException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John", "john@example.com");
        userDto = new UserDto(1L, "John", "john@example.com");
    }

    @Test
    void create_shouldSaveUser_whenEmailIsUnique() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userDto);

        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_shouldThrowConflict_whenEmailAlreadyExists() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        assertThrows(ConflictException.class, () -> userService.create(userDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_shouldUpdateNameAndEmail() {
        User updatedUser = new User(1L, "Updated", "updated@example.com");
        UserDto updateDto = new UserDto(null, "Updated", "updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.update(1L, updateDto);

        assertEquals("Updated", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(1L, userDto));
    }

    @Test
    void update_shouldThrowConflict_whenEmailAlreadyUsedByAnotherUser() {
        UserDto updateDto = new UserDto(null, "Another", "someone@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(List.of(user, new User(2L, "User2", "someone@example.com")));

        assertThrows(ConflictException.class, () -> userService.update(1L, updateDto));
    }

    @Test
    void update_shouldOnlyUpdateName_whenEmailIsNull() {
        UserDto updateDto = new UserDto(null, "NewName", null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.update(1L, updateDto);

        assertEquals("NewName", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void getById_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getById(1L);

        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
    }

    @Test
    void getById_shouldThrowNotFound_whenUserNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(1L));
    }

    @Test
    void getAll_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAll();

        assertEquals(1, result.size());
        assertEquals(user.getEmail(), result.get(0).getEmail());
    }

    @Test
    void delete_shouldRemoveUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowNotFound_whenUserDoesNotExist() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.delete(1L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        assertTrue(userService.existsById(1L));
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertFalse(userService.existsById(1L));
    }
}