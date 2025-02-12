package com.libraryManagement.controller;

import com.libraryManagement.DTO.LoginRequestDto;
import com.libraryManagement.DTO.LoginResponseDto;
import com.libraryManagement.Exception.ResourceNotFoundException;
import com.libraryManagement.Util.JwtUtil;
import com.libraryManagement.service.CustomUserDetail;
import com.libraryManagement.service.CustomUserDetailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;

    public AuthenticationController(AuthenticationManager authenticationManager, CustomUserDetailService customUserDetailService, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.customUserDetailService = customUserDetailService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> userAuthentication(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDto.username(), loginRequestDto.password()));

            CustomUserDetail userDetails = customUserDetailService.loadUserByUsername(loginRequestDto.username());
            String generatedToken = jwtUtil.generateToken(userDetails);
            LoginResponseDto responseDto = new LoginResponseDto(generatedToken, userDetails.getUserId(), userDetails.getRole().name());
            return ResponseEntity.ok(responseDto);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Username or Password..!");
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during authentication");
        }
    }
}