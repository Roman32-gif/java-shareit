package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User createUser(User user);

    User updateUser(User user);

    void deleteUser(Long userId);

    Optional<User> findUserById(Long userId);

    List<User> getUsers();

    Optional<User> findUserByEmail(String email);
}
