package com.iris.backend.service.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Thrown when a request arrives without a valid JWT token.
 *
 * <p>Raised by {@code AuthInterceptor} in these cases:
 * <ul>
 *   <li>No {@code Authorization} header</li>
 *   <li>Token signature is invalid</li>
 *   <li>Token is expired</li>
 *   <li>User ID from token does not exist in the database</li>
 * </ul>
 *
 * <p>Resolves to {@code 401 Unauthorized}.
 */
public class UnauthorizedException extends ResponseStatusException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
