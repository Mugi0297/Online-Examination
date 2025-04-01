package com.exam.service;

import com.exam.model.User;

public interface UserService {
    User registerUser(User user);
    boolean verifyEmail(String code);
    User findByEmail(String email);
}
