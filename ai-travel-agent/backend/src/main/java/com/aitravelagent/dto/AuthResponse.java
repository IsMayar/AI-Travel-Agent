package com.aitravelagent.dto;

public record AuthResponse(
        String token,
        AuthUserResponse user
) {
}
