package com.keycloak.keycloak_springboot.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private String userName;

    private String role;

}
