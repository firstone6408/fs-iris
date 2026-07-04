package com.iris.backend.service.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown when login fails due to incorrect email or password.
 *
 * <p>The message is intentionally generic ("Invalid email or password") to avoid
 * revealing whether the email exists in the system.
 *
 * <p>Resolves to {@code 401 Unauthorized}.
 */
public class InvalidCredentialsException extends ResponseStatusException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
