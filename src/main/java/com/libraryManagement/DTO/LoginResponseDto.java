package com.libraryManagement.DTO;

public record LoginResponseDto (
        String token,
        Long userId,
        String role
){

}
