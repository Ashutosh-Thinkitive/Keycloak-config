package com.keycloak.keycloak_springboot.controller;

import com.keycloak.keycloak_springboot.model.Role;
import com.keycloak.keycloak_springboot.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;


    @GetMapping
    public List<Role> getRole() {
        return roleService.getRole();
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @GetMapping("/{roleName}")
    public Role getRoleByName(@PathVariable String roleName) {
        return roleService.getRoleByName(roleName);
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping
    public ResponseEntity<String> createRole(@RequestBody Role role) {
        return roleService.createRole(role);
    }

    @PreAuthorize("hasRole('ROLE_admin')")
    @DeleteMapping("/{roleName}")
    public ResponseEntity<String> deleteRole(@PathVariable String roleName) {
        return roleService.deleteRole(roleName);
    }

}
