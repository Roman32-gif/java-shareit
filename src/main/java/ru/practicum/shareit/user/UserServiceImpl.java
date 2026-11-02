package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.AlreadyExistsException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Override
    public UserDto addUser(UserDto userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("Введены некорректные данные");
        }
        checkEmail(userDto.getEmail(),null);
        User user = userStorage.createUser(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUser(Long id) {
        return UserMapper.toUserDto(getUserOrThrow(id));
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userStorage.getUsers().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto updateUser(UserDto userDto, Long id) {
        if (userDto == null) {
            throw new IllegalArgumentException("Введены некорректные данные");
        }
        User user = getUserOrThrow(id);

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            checkEmail(userDto.getEmail(), user.getId());
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }

        User updated = userStorage.updateUser(user);
        return UserMapper.toUserDto(updated);
    }

    @Override
    public void deleteUser(Long id) {
        getUserOrThrow(id);
        userStorage.deleteUser(id);
    }

    private User getUserOrThrow(Long id) {
        return userStorage.findUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    private void checkEmail(String email, Long userId) {
        userStorage.findUserByEmail(email)
                .filter(f -> !f.getId().equals(userId))
                .ifPresent(f -> {
                    throw new AlreadyExistsException("Пользователь с такой почтой уже существует");
                });
    }
}
