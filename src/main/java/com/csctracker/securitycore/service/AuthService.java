package com.csctracker.securitycore.service;

import com.csctracker.dto.Conversor;
import com.csctracker.dto.TokenDTO;
import com.csctracker.dto.UserDTO;
import com.csctracker.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {


    private final Conversor<User, UserDTO> conversor;
    @Value("${auth.ip:#{\"127.0.0.1\"}}")
    private String ipAuth;
    @Value("${auth.port:${server.port}}")
    private String portAuth;
    @Value("${auth.redirect_uri}")
    private String redirectUri;

    public AuthService() {
        this.conversor = new Conversor<>(User.class, UserDTO.class);
    }

    public TokenDTO getToken(String autorazationCode, String url) throws UnirestException, JsonProcessingException {
        var response = Unirest.get("http://" + ipAuth + ":" + portAuth + "/oauth")
                .header("Content-Type", "application/json")
                .queryString("code", autorazationCode)
                .queryString("redirect_uri", redirectUri)
                .asString();

        var user = conversor.toD(response.getBody());
        return user.getToken();
    }

    public TokenDTO getToken(String autorazationCode, String url, String tokenGoogleSt) throws UnirestException, JsonProcessingException {
        var response = Unirest.post("http://" + ipAuth + ":" + portAuth + "/oauth")
                .header("Content-Type", "application/json")
                .queryString("code", autorazationCode)
                .queryString("redirect_uri", url)
                .queryString("tokenGoogle", tokenGoogleSt)
                .asString();

        var user = conversor.toD(response.getBody());
        return user.getToken();
    }
}
