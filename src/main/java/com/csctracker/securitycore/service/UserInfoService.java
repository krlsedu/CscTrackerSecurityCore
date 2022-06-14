package com.csctracker.securitycore.service;


import com.csctracker.configs.UnAuthorized;
import com.csctracker.model.User;
import com.csctracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public String getEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UnAuthorized("No user is logged in");
        }
        return authentication.getName();
    }

    public User getUser() {
        return userRepository.findByEmail(getEmail());
    }
}
