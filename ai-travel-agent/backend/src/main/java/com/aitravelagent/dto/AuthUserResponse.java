package com.aitravelagent.dto;

public record AuthUserResponse(
        Long id,
        String fullName,
        String email
) {
}
