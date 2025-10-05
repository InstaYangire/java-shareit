package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.ConflictException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public UserDto create(UserDto userDto) {

        // email uniqueness check
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            throw new ConflictException("Email already in use: " + userDto.getEmail());
        }

        User user = UserMapper.toUser(userDto);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existing = users.get(id);
        if (existing == null) {
            throw new NotFoundException("User with id " + id + " not found");
        }

        // checking email if it changes
        if (userDto.getEmail() != null && !userDto.getEmail().equals(existing.getEmail())) {
            if (users.values().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
                throw new ConflictException("Email already in use: " + userDto.getEmail());
            }
            existing.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }

        return UserMapper.toUserDto(existing);
    }

    @Override
    public UserDto getById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("User with id " + id + " not found");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }
        users.remove(id);
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }
}