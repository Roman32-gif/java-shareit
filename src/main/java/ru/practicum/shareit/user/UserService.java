package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto addUser(UserDto user);

    UserDto getUser(Long id);

    List<UserDto> getAllUsers();

    UserDto updateUser(UserDto user, Long id);

    void deleteUser(Long id);
}
