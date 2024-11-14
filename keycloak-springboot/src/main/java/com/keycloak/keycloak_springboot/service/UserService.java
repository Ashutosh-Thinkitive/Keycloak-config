package com.keycloak.keycloak_springboot.service;

import com.keycloak.keycloak_springboot.model.User;
import com.keycloak.keycloak_springboot.model.UserDto;
import com.keycloak.keycloak_springboot.repository.UserRepository;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KeycloakSecurityUtil keycloakSecurityUtil;

    @Value("${realm}")
    private String realm;


    public List<UserDto> getUsers() {
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
        List<UserRepresentation> userRepresentations = keycloak.realm(realm).users().list();
        return mapUsers(userRepresentations);

    }

    public ResponseEntity<UserDto> createUser(UserDto userDto, String role) {
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();

        RoleRepresentation roleRepresentation = getRoleByName(keycloak, role);
        if (roleRepresentation == null) {
            System.out.println("Role " + role + " not found in realm.");
            return new ResponseEntity<>(userDto, HttpStatus.BAD_REQUEST);
        }

        UserRepresentation userRep = mapUserRep(userDto);
        Response response = keycloak.realm(realm).users().create(userRep);


        if (response.getStatus() == 201) {
            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            userDto.setId(userId);
            assignRoleToUser(keycloak, userId, role);

            User user = mapUsedtoToUser(userDto, userId, role);
            userRepository.save(user);

            return new ResponseEntity<>(userDto, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(userDto, HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<String> deleteUser(String userId) {
        Optional<User> user = userRepository.findByUserId(userId);
        if (user.isPresent()) {
            try {
                Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
                keycloak.realm(realm).users().get(userId).remove();
                userRepository.delete(user.get());
                return new ResponseEntity<>("User deleted successfully", HttpStatus.OK);

            } catch (Exception e) {
                return new ResponseEntity<>("Failed to delete user from Keycloak: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

            }
        }
        return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);

    }


    private void assignRoleToUser(Keycloak keycloak, String userId, String roleName) {
        RoleRepresentation role = getRoleByName(keycloak, roleName);
        if (role != null) {
            keycloak.realm(realm).users().get(userId).roles().realmLevel().add(List.of(role));
        } else {
            System.out.println("Role " + roleName + " not found in realm.");
            throw new RuntimeException("Role not found");
        }
    }


    private RoleRepresentation getRoleByName(Keycloak keycloak, String roleName) {
        List<RoleRepresentation> roles = keycloak.realm(realm).roles().list();
        for (RoleRepresentation role : roles) {
            if (role.getName().equals(roleName)) {
                return role;
            }
        }
        return null;  // Role doesn't exist
    }


    private List<UserDto> mapUsers(List<UserRepresentation> userRep) {
        List<UserDto> users = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(userRep)) {
            userRep.forEach(u -> {
                        users.add(mapUser(u));
                    }
            );
        }
        return users;
    }

    private UserDto mapUser(UserRepresentation userRep) {
        UserDto user = new UserDto();
        user.setId(userRep.getId());
        user.setFirstName(userRep.getFirstName());
        user.setLastName(userRep.getLastName());
        user.setEmail(userRep.getEmail());
        user.setUserName(userRep.getUsername());
        return user;
    }


    private UserRepresentation mapUserRep(UserDto user) {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setFirstName(user.getFirstName());
        userRep.setLastName(user.getLastName());
        userRep.setEmail(user.getEmail());
        userRep.setUsername(user.getUserName());
        System.out.println("user username" + user.getUserName());
        System.out.println("=============");
        System.out.println("userRep username" + userRep.getUsername());
        userRep.setEnabled(true);
        userRep.setEmailVerified(true);
        List<CredentialRepresentation> credentials = new ArrayList<>();
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setTemporary(false); // updated password false;
        cred.setValue(user.getPassword());
        credentials.add(cred);
        userRep.setCredentials(credentials);
        return userRep;
    }

    private User mapUsedtoToUser(UserDto userDto, String userId, String role) {
        User user = new User();

        user.setUserId(userId);
        user.setUserName(userDto.getUserName());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setRole(role);

        return user;
    }


}
