package com.game.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RegisterRequest {
    private String username;
    private String password;
    private String role; // "ADMIN" or "PLAYER"
}
