package com.iris.backend.service.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown when a register or update request uses an email that is already registered.
 *
 * <p>Resolves to {@code 409 Conflict}.
 */
public class EmailAlreadyTakenException extends ResponseStatusException {

    public EmailAlreadyTakenException(String email) {
        super(HttpStatus.CONFLICT, "Email already taken: " + email);
    }
}
