package com.keycloak.keycloak_springboot.config;

//import org.keycloak.adapters.authorization.integration.jakarta.ServletPolicyEnforcerFilter;
//import org.keycloak.adapters.authorization.spi.ConfigurationResolver;
//import org.keycloak.adapters.authorization.spi.HttpRequest;
//import org.keycloak.representations.adapters.config.PolicyEnforcerConfig;
//import org.keycloak.util.JsonSerialization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JWTAuthenticator jwtAuthenticator;

    //    OAUTH2 LEVEL AUTHENTICATION AND AUTHORIZATION
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> {
                            auth
                                    .anyRequest().authenticated();
                        }
                );
        http.oauth2ResourceServer(oAuth2 -> oAuth2.jwt(configurer -> configurer.jwtAuthenticationConverter(jwtAuthenticator)));

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }


//*********************************************************************************************************************************


    // DefaultMethodSecurityExpressionHandler this class implicitly check for ROLE_ prefix any role appended
    // to remove this prefix use this bean
//    @Bean
//    public DefaultMethodSecurityExpressionHandler msecurity(){
//        DefaultMethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler = new DefaultMethodSecurityExpressionHandler();
//        defaultMethodSecurityExpressionHandler.setDefaultRolePrefix("");
//        return defaultMethodSecurityExpressionHandler;
//    }


//**********************************************************************************************************************************


    // KEYCLOAK LEVEL AUTHENTICATION AND AUTHORIZATION
    // Don't need to add any preAuthorize role keycloak handles

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable());
//        http.addFilterAfter(createPolicyEnforce(), BearerTokenAuthenticationFilter.class);
//        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//        return http.build();
//    }
//
//    private ServletPolicyEnforcerFilter createPolicyEnforce() {
//        return new ServletPolicyEnforcerFilter(new ConfigurationResolver() {
//            @Override
//            public PolicyEnforcerConfig resolve(HttpRequest httpRequest) { // we are overriding PolicyEnforcerConfig because we are telling ServletPolicyEnforcerFilter that we have keycloak specific configuration and we want to integrate that for PolicyEnforcerConfig
//
//                try {
//                    return JsonSerialization.readValue(getClass().getResourceAsStream("/policy-enforcer.json"), PolicyEnforcerConfig.class); // mapping PolicyEnforcerConfig class with json file
//
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        });
//    }


}
