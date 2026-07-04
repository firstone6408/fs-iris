package com.iris.backend.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Getter;

/** All fields are optional — only non-null values are applied on update. */
@Getter
public class UpdateUserRequestDTO {

    @Email(message = "Invalid email format")
    private String email;

    private String name;
    private String displayName;
    private String avatarUrl;
    private String password;
}
