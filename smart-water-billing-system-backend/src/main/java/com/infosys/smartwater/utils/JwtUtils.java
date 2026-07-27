package com.infosys.smartwater.utils;

import com.infosys.smartwater.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Stateless utility bean for JWT creation, parsing, and validation.
 *
 * <p>Uses the <a href="https://github.com/jwtk/jjwt">JJWT 0.12.x</a> fluent API.
 * The signing key is an HMAC-SHA key derived from the Base64-encoded secret
 * in {@link JwtProperties}.
 *
 * <p><b>Token structure:</b>
 * <ul>
 *   <li>Subject ({@code sub}): user email (used as the principal identity)</li>
 *   <li>Issued-at ({@code iat}): token creation timestamp</li>
 *   <li>Expiration ({@code exp}): creation + {@code application.jwt.expiration}</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;

    // -------------------------------------------------------------------------
    // Token generation
    // -------------------------------------------------------------------------

    /**
     * Generates a JWT access token for the given user (no extra claims).
     *
     * @param userDetails the authenticated principal (username = email)
     * @return a signed, compact JWT string
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT access token with additional custom claims.
     *
     * @param extraClaims additional claims to embed in the payload (e.g., {@code role})
     * @param userDetails the authenticated principal
     * @return a signed, compact JWT string
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())          // subject = email
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.getExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    // -------------------------------------------------------------------------
    // Claim extraction
    // -------------------------------------------------------------------------

    /**
     * Extracts the subject (email / username) from the token.
     *
     * @param token the JWT string (without "Bearer " prefix)
     * @return the subject claim value
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration timestamp from the token.
     *
     * @param token the JWT string
     * @return the expiration {@link Date}
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a single claim from the token using a resolver function.
     *
     * @param token          the JWT string
     * @param claimsResolver a function mapping {@link Claims} to the desired value
     * @param <T>            the type of the claim value
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    /**
     * Validates the token: checks the signature, expiration, and that the
     * subject matches the supplied {@link UserDetails}.
     *
     * @param token       the JWT string to validate
     * @param userDetails the expected principal
     * @return {@code true} if the token is valid for this user and not expired
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Returns {@code true} if the token's expiration timestamp is in the past.
     *
     * @param token the JWT string
     * @return {@code true} if expired
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Parses the JWT and returns all claims from the payload.
     *
     * @param token the JWT string
     * @return parsed {@link Claims}
     * @throws JwtException if the token is malformed, expired, or the signature is invalid
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Derives the HMAC-SHA {@link SecretKey} from the Base64-encoded secret
     * configured in {@link JwtProperties}.
     *
     * @return the signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
