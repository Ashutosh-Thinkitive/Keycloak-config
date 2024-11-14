package com.keycloak.keycloak_springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.core.userdetails.User;
import org.springframework.core.convert.converter.Converter;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;


import java.util.*;

@Component
public class JWTAuthenticator implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> roles = extractAuthorities(jwt);
        System.out.println("Extracted roles: " + roles);  // Debug output

        return new JwtAuthenticationToken(jwt, roles);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        if (jwt.getClaim("realm_access") != null) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            ObjectMapper mapper = new ObjectMapper();
            List<String> keyCloakRoles = mapper.convertValue(realmAccess.get("roles"), new TypeReference<List<String>>() {
            });
            List<GrantedAuthority> roles = new ArrayList<>();
            for (String keycloakRole : keyCloakRoles) {
                roles.add(new SimpleGrantedAuthority("ROLE_" + keycloakRole));
            }
            System.out.println(roles);
            return roles;
        }
        return new ArrayList<>();
    }



}
