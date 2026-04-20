package com.manager.account.application.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class TokenResponse {
    private String code;
    private String message;
    private String token;
}




