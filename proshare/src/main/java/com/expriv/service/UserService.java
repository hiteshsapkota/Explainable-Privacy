package com.expriv.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.expriv.model.User;
import com.expriv.web.dto.UserRegistrationDto;

public interface UserService extends UserDetailsService {

    User findByEmail(String email);

    User save(UserRegistrationDto registration);
    void updatePassword(String password, Long userId);
}
