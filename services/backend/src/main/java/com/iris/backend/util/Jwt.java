package com.iris.backend.util;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT utility for generating and validating tokens.
 *
 * <p>Reads {@code jwt.secret} and {@code jwt.expiration} from application properties.
 * Tokens store an arbitrary string in the subject claim (typically a user ID).
 */
@Component
public class Jwt {

    @Value("${jwt.secret}")
    private String secret;

    /** Token lifetime in milliseconds. Example: {@code 86400000} = 24 hours. */
    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigninKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a signed JWT containing the given data as the subject claim.
     *
     * @param data arbitrary string to store in the token (e.g. user ID or email)
     * @return compact JWT string
     */
    public String generateToken(String data) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(data)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigninKey())
                .compact();
    }

    /**
     * Extracts the subject claim from a valid, non-expired token.
     *
     * @param token compact JWT string
     * @return the value stored in the subject claim
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public String getDataFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigninKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Returns {@code true} if the token has a valid signature and is not expired.
     *
     * @param token compact JWT string
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigninKey())
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns {@code true} if the token is expired.
     *
     * <p>Returns {@code false} for tokens that are still valid or are malformed
     * (i.e. expiration cannot be determined).
     *
     * @param token compact JWT string
     * @return {@code true} if expired, {@code false} if still valid or unparseable
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigninKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
