package com.libraryManagement.service;

import com.libraryManagement.model.User;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    List<User> getAllUsers(int page, int size);
    User updateUser(Long id, User updatedUser);
    void deleteUser(Long id);
}
