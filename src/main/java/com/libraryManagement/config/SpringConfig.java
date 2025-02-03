package com.libraryManagement.config;

import com.libraryManagement.service.CustomUserDetailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringConfig {

    private final CustomUserDetailService customUserDetailService;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    public SpringConfig(CustomUserDetailService customUserDetailService, CustomAccessDeniedHandler accessDeniedHandler) {
        this.customUserDetailService = customUserDetailService;
        this.accessDeniedHandler = accessDeniedHandler;
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request->request
                .requestMatchers( "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,"/api/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,"/api/books/**").hasAnyRole("ADMIN", "MEMBER")
                .requestMatchers("/api/borrow/**").hasAnyRole("ADMIN", "MEMBER")
                .anyRequest().authenticated())
                .exceptionHandling(exception->exception.accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults()).build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider()
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
