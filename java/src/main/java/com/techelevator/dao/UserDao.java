package com.techelevator.dao;

import com.techelevator.model.auth.RegisterUserDto;
import com.techelevator.model.auth.User;

import java.util.List;

public interface UserDao {

    List<User> getUsers();

    User getUserById(int id);

    User getUserByUsername(String username);

    User createUser(RegisterUserDto user);

    int findIdByUsername(String username);

    void updateProfile(int userId, String fullName, String phoneNumber);
}
