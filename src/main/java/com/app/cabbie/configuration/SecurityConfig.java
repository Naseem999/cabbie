package com.app.cabbie.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity(debug = true)
public class SecurityConfig {


    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private JWTFilter jwtFilter;




    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
      return   http
              .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ← add this
              .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/api/users/register").permitAll()
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "PASSENGER", "DRIVER")
                        .requestMatchers("/api/users/logout").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }



    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://127.0.0.1:5500",   // ← VS Code Live Server
                "http://localhost:5500",
                "http://localhost:8080"

        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


}
