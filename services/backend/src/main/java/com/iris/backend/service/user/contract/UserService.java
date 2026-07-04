package com.iris.backend.service.user.contract;

import java.util.List;

import com.iris.backend.dto.user.UpdateUserRequestDTO;
import com.iris.backend.dto.user.UserResponseDTO;

/**
 * User profile operations for the currently authenticated user.
 *
 * <p>All methods operate on the user resolved from the request context
 * ({@link com.iris.backend.context.user.UserContextProvider}) — no user ID
 * is accepted as a parameter to prevent users from accessing or modifying other accounts.
 */
public interface UserService {

    /**
     * Returns all users ordered by email ascending.
     *
     * @return list of all users
     */
    List<UserResponseDTO> getAll();

    /**
     * Returns the profile of the currently authenticated user.
     *
     * @return the current user's data
     */
    UserResponseDTO getCurrentUser();

    /**
     * Updates the profile of the currently authenticated user.
     *
     * <p>Only non-null fields in the request are applied. Password is re-hashed before saving.
     *
     * @param request fields to update (all optional)
     * @return the updated user data
     * @throws com.iris.backend.service.auth.exception.EmailAlreadyTakenException if the new email is already used by another account
     */
    UserResponseDTO update(UpdateUserRequestDTO request);
}
