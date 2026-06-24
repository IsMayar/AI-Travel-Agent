package com.aitravelagent.dto;

public record RegisterRequest(
        String fullName,
        String email,
        String password
) {
}
