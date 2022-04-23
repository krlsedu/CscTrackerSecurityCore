package com.csctracker.securitycore.service;


import com.csctracker.securitycore.model.User;
import com.csctracker.securitycore.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
public class UserInfoService {

    private final UserRepository userRepository;

    public UserInfoService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private String getEmail(Principal principal) {
        return principal.getName();
    }

    public User getUser(Principal principal) {
        return userRepository.findByEmail(getEmail(principal));
    }
}
