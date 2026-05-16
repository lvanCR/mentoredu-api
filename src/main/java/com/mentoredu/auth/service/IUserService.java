package com.mentoredu.auth.service;

import com.mentoredu.auth.dto.RegisterRequest;
import com.mentoredu.auth.model.User;

public interface IUserService {
    User register(RegisterRequest request);
    User login(String email, String password);
}