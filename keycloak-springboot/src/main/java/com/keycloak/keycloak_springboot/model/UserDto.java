package com.keycloak.keycloak_springboot.model;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class UserDto {


    private String id;

    private String firstName;

    private String lastName;

    private String email;

    private String userName;

    private String password;

}
