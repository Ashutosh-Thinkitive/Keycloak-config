package com.keycloak.keycloak_springboot.model;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}
