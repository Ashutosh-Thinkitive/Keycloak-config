package com.keycloak.keycloak_springboot.controller;

import com.keycloak.keycloak_springboot.model.User;
import com.keycloak.keycloak_springboot.model.UserDto;
import com.keycloak.keycloak_springboot.service.UserService;
import com.keycloak.keycloak_springboot.util.KeycloakSecurityUtil;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {


    @Autowired
    private UserService userService;


    @GetMapping
    public List<UserDto> getUsers() {

        return userService.getUsers();
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping("/admin/{role}")
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto, @PathVariable String role) {
        return userService.createUser(userDto, role);
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @DeleteMapping("/admin/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId){
        return userService.deleteUser(userId);
    }

}
