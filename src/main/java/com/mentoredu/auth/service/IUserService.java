package com.mentoredu.auth.service;

import com.mentoredu.auth.model.User;

public interface IUserService {
    User register(User user);
    User login(String email, String password);
}