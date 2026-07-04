package com.iris.backend.context.user;

import com.iris.backend.entity.UserEntity;

/** Provides access to the authenticated user for the current request. */
public interface UserContextProvider {

    UserEntity getCurrentUser();
}
