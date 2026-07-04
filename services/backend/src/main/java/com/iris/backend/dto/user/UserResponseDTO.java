package com.iris.backend.dto.user;

import java.time.LocalDateTime;

import com.iris.backend.entity.UserEntity;

import lombok.Getter;

@Getter
public class UserResponseDTO {

    private final String id;
    private final String email;
    private final String name;
    private final String displayName;
    private final String avatarUrl;
    private final LocalDateTime createdAt;

    private UserResponseDTO(String id, String email, String name, String displayName, String avatarUrl,
            LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    public static UserResponseDTO fromEntity(UserEntity user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getCreatedAt());
    }
}
