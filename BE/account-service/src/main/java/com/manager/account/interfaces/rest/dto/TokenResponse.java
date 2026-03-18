package com.manager.account.interfaces.rest.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class TokenResponse {
    private String code;
    private String message;
    private String token;
}




