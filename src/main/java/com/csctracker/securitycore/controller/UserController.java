package com.csctracker.securitycore.controller;

import com.csctracker.dto.Conversor;
import com.csctracker.dto.UserDTO;
import com.csctracker.model.User;
import com.csctracker.securitycore.service.UserInfoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class UserController {
    private final UserInfoService userInfoService;

    private final Conversor<User, UserDTO> conversor;

    public UserController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
        this.conversor = new Conversor<>(User.class, UserDTO.class);
    }

    @GetMapping("/user")
    public ResponseEntity<UserDTO> getEmail(Principal principal) throws JsonProcessingException {
        UserDTO userDTO = conversor.toD(userInfoService.getUser(principal));
        userDTO.setId(null);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }
}
