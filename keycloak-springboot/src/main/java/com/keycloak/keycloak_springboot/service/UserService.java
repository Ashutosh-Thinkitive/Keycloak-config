package com.keycloak.keycloak_springboot.service;

import com.keycloak.keycloak_springboot.model.LoginRequest;
import com.keycloak.keycloak_springboot.model.User;
import com.keycloak.keycloak_springboot.model.UserDto;
import com.keycloak.keycloak_springboot.repository.UserRepository;
import com.keycloak.keycloak_springboot.util.KeycloakSecurityUtil;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
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

    @Value("${server-url}")
    private String authServerUrl;


    @Value("${client}")
    private String clientId;


    @Value("${client-secret}")
    private String clientSecret;

    public List<UserDto> getUsers() {
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
        List<UserRepresentation> userRepresentations = keycloak.realm(realm).users().list();
        return mapUsers(userRepresentations);

    }

    public ResponseEntity<?> login(LoginRequest loginRequest) {

        try {
            String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);

            HttpPost httpPost = new HttpPost(tokenUrl);

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "password"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("username", loginRequest.getUsername()));
            params.add(new BasicNameValuePair("password", loginRequest.getPassword()));

            httpPost.setEntity(new UrlEncodedFormEntity(params));
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                CloseableHttpResponse response = httpClient.execute(httpPost);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String responseString = EntityUtils.toString(response.getEntity());
                    return new ResponseEntity<>(responseString, HttpStatus.OK);
                } else {
                    return ResponseEntity.status(response.getStatusLine().getStatusCode())
                            .body("Login failed: " + response.getStatusLine().getReasonPhrase());

                }
            }


        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }

    }

    public ResponseEntity<UserDto> createUser(UserDto userDto, String role) {
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();

        RoleRepresentation roleRepresentation = getRoleByName(keycloak, role);
        if (roleRepresentation == null) {
            System.out.println("Role " + role + " not found in realm.");
            return new ResponseEntity<>(userDto, HttpStatus.BAD_REQUEST);
        }

        // Check email already exists
        List<UserRepresentation> existingUsers = keycloak.realm(realm).users().search(null, null, null, userDto.getEmail(), 0, 1);
        if (!existingUsers.isEmpty()) {
            System.out.println("User with email " + userDto.getEmail() + " already exists.");
            return new ResponseEntity<>(userDto, HttpStatus.CONFLICT);
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


    public ResponseEntity<String> updateUser(String userId, UserDto userDto) {
        try {
            Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
            UserResource userResource = keycloak.realm(realm).users().get(userId);

            UserRepresentation userRep = userResource.toRepresentation();

            userRep.setFirstName(userDto.getFirstName());
            userRep.setLastName(userDto.getLastName());
            userRep.setEmail(userDto.getEmail());
            userRep.setEnabled(true);
            userRep.setEmailVerified(true);


            // Update in Keycloak
            userResource.update(userRep);

            Optional<User> existingUserOptional = userRepository.findByUserId(userId);
            if (existingUserOptional.isPresent()) {
                User existingUser = existingUserOptional.get();
                existingUser.setFirstName(userDto.getFirstName());
                existingUser.setLastName(userDto.getLastName());
                existingUser.setEmail(userDto.getEmail());
                userRepository.save(existingUser);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            userDto.setId(userId);
            return new ResponseEntity<>("User updated successfully", HttpStatus.OK);

        } catch (BadRequestException e) {
            System.out.println("Bad Request: " + e.getMessage());
            return new ResponseEntity<>("Bad Request: Invalid data", HttpStatus.BAD_REQUEST);
        } catch (NotFoundException e) {
            return new ResponseEntity<>("User not found in Keycloak", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
