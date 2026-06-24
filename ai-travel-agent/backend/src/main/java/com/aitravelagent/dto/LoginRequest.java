package com.aitravelagent.dto;

public record LoginRequest(
        String email,
        String password
) {
}
