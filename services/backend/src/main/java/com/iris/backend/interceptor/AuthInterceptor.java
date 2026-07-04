package com.iris.backend.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts incoming requests to verify the JWT bearer token.
 *
 * <p>Routes registered in {@code WebConfig.addInterceptors()} pass through here
 * before reaching any controller. Requests without a valid token are rejected
 * with {@code 401 Unauthorized}.
 *
 * <p>Implementation is pending.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

}
