package com.iris.backend.service.auth.contract;

import com.iris.backend.dto.auth.LoginRequestDTO;
import com.iris.backend.dto.auth.LoginResponseDTO;
import com.iris.backend.dto.auth.RegisterRequestDTO;
import com.iris.backend.dto.user.UserResponseDTO;

/**
 * Authentication operations — register and login.
 *
 * <p>Implementations are responsible for password hashing, duplicate email checks,
 * and JWT token generation. Business rules are enforced here, not in the controller.
 */
public interface AuthService {

    /**
     * Creates a new user account.
     *
     * @param request registration data (email, name, password)
     * @return the created user
     * @throws com.iris.backend.service.auth.exception.EmailAlreadyTakenException if the email is already registered
     */
    UserResponseDTO register(RegisterRequestDTO request);

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request login credentials (email, password)
     * @return a response containing the signed JWT token
     * @throws com.iris.backend.service.auth.exception.InvalidCredentialsException if email not found or password does not match
     */
    LoginResponseDTO login(LoginRequestDTO request);
}
