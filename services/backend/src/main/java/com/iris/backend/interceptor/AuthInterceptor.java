package com.iris.backend.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.iris.backend.context.user.implement.ThreadLocalUserContextProvider;
import com.iris.backend.entity.UserEntity;
import com.iris.backend.repository.UserRepository;
import com.iris.backend.service.auth.exception.UnauthorizedException;
import com.iris.backend.util.Jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Intercepts incoming requests to verify the JWT bearer token.
 *
 * <p>Extracts the token from the {@code Authorization: Bearer <token>} header,
 * validates it, loads the user from the database, and stores them in
 * {@link ThreadLocalUserContextProvider} for use by downstream services.
 *
 * <p>Always clears the thread-local user in {@code afterCompletion} to prevent leaks.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final Jwt jwt;
    private final UserRepository userRepository;
    private final ThreadLocalUserContextProvider userContextProvider;

    public AuthInterceptor(Jwt jwt, UserRepository userRepository,
            ThreadLocalUserContextProvider userContextProvider) {
        this.jwt = jwt;
        this.userRepository = userRepository;
        this.userContextProvider = userContextProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new UnauthorizedException("Authorization header missing or malformed");
            }

            String token = authHeader.substring(7);

            if (!jwt.isTokenValid(token)) {
                throw new UnauthorizedException("Invalid token");
            }

            if (jwt.isTokenExpired(token)) {
                throw new UnauthorizedException("Token expired");
            }

            String userId = jwt.getDataFromToken(token);
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            userContextProvider.setCurrentUser(user);
            return true;
        } catch (Exception e) {
            userContextProvider.clear();
            throw e;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        userContextProvider.clear();
    }
}
