package com.iris.backend.context.user.implement;

import org.springframework.stereotype.Component;

import com.iris.backend.context.user.UserContextProvider;
import com.iris.backend.entity.UserEntity;

/**
 * Stores the authenticated user for the current request thread using {@link ThreadLocal}.
 *
 * <p>Set by {@code AuthInterceptor.preHandle()} and cleared in {@code afterCompletion()}
 * to prevent leaks across requests.
 */
@Component
public class ThreadLocalUserContextProvider implements UserContextProvider {

    private static final ThreadLocal<UserEntity> userHolder = new ThreadLocal<>();

    /** Called by {@code AuthInterceptor} only — sets the user for this thread. */
    public void setCurrentUser(UserEntity user) {
        userHolder.set(user);
    }

    @Override
    public UserEntity getCurrentUser() {
        UserEntity user = userHolder.get();
        if (user == null) {
            throw new IllegalStateException("No authenticated user in context");
        }
        return user;
    }

    /** Called by {@code AuthInterceptor} in afterCompletion — always clears the thread. */
    public void clear() {
        userHolder.remove();
    }
}
