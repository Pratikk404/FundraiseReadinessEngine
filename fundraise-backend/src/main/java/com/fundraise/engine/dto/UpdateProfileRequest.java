package com.fundraise.engine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100)
    private String name;

    @Email(message = "Must be a valid email")
    private String email;
}
