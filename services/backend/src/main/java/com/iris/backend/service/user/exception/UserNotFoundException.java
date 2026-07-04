package com.iris.backend.service.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown when a user lookup by ID returns no result.
 *
 * <p>Resolves to {@code 404 Not Found}.
 */
public class UserNotFoundException extends ResponseStatusException {

    public UserNotFoundException(String userId) {
        super(HttpStatus.NOT_FOUND, "User not found: " + userId);
    }
}
