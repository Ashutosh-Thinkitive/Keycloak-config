package com.keycloak.keycloak_springboot.service;

import com.keycloak.keycloak_springboot.model.Role;
import com.keycloak.keycloak_springboot.util.KeycloakSecurityUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.common.util.CollectionUtil;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleService {


    @Autowired
    private KeycloakSecurityUtil keycloakSecurityUtil;

    @Value("${realm}")
    private String realm;

    public List<Role> getRole() {
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
        List<RoleRepresentation> representations = keycloak.realm(realm).roles().list();
        return mapRoles(representations);
    }

    public Role getRoleByName(String roleName) {
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
        return mapRole(keycloak.realm(realm).roles().get(roleName).toRepresentation());
    }

    public ResponseEntity<String> createRole(Role role) {

        RoleRepresentation roleRep = mapRoleRep(role);
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
        keycloak.realm(realm).roles().create(roleRep);
        return new ResponseEntity<>("Role Created successfully", HttpStatus.CREATED);
    }

    public ResponseEntity<String> deleteRole(String roleName) {
        Keycloak keycloak = keycloakSecurityUtil.getKeycloakInstance();
        keycloak.realm(realm).roles().deleteRole(roleName);
        return new ResponseEntity<>("Role deleted successfully", HttpStatus.OK);
    }


    public List<Role> mapRoles(List<RoleRepresentation> roleRep) {
        List<Role> roles = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(roleRep)) {
            roleRep.forEach(r -> roles.add(mapRole(r)));
        }
        return roles;
    }

    public Role mapRole(RoleRepresentation roleRep) {
        Role role = new Role();
        role.setId(roleRep.getId());
        role.setName(roleRep.getName());
        return role;
    }

    public RoleRepresentation mapRoleRep(Role role) {
        RoleRepresentation roleRep = new RoleRepresentation();
        roleRep.setName(role.getName());
        return roleRep;
    }
}
