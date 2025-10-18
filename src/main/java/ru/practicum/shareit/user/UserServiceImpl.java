package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.ConflictException;
import ru.practicum.shareit.error.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDto create(UserDto userDto) {
        if (userRepository.findAll().stream()
                .anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            throw new ConflictException("Email already in use: " + userDto.getEmail());
        }

        User user = UserMapper.toUser(userDto);
        User saved = userRepository.save(user);
        return UserMapper.toUserDto(saved);
    }

    @Transactional
    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existing.getEmail())) {
            if (userRepository.findAll().stream()
                    .anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
                throw new ConflictException("Email already in use: " + userDto.getEmail());
            }
            existing.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existing.setName(userDto.getName());
        }

        return UserMapper.toUserDto(userRepository.save(existing));
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }
}