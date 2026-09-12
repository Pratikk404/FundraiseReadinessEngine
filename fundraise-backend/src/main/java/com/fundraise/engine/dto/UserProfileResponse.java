package com.fundraise.engine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String plan;
    private boolean verified;
}
