package com.infosys.smartwater.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed binding for all {@code application.jwt.*} properties
 * declared in {@code application.yml}.
 *
 * <p>Inject this bean into any component that needs JWT configuration
 * instead of reading raw {@code @Value} strings.
 *
 * <p>Property map:
 * <pre>
 * application:
 *   jwt:
 *     secret:             Base64-encoded HMAC-SHA signing key (≥256 bits)
 *     expiration:         Access token validity in milliseconds (default 24 h)
 *     refresh-expiration: Refresh token validity in milliseconds (default 7 d)
 *     token-prefix:       "Bearer " (note trailing space)
 *     header-name:        "Authorization"
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "application.jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * Base64-encoded HMAC-SHA signing secret.
     *
     * <p><b>⚠ Security:</b> Generate with {@code openssl rand -base64 64}
     * and supply via an environment variable or secret manager in production.
     * Never commit raw secrets to source control.
     */
    private String secret;

    /**
     * Access token validity in milliseconds.
     * Default: {@code 86400000} (24 hours).
     */
    private long expiration = 86_400_000L;

    /**
     * Refresh token validity in milliseconds.
     * Default: {@code 604800000} (7 days).
     * Reserved for future refresh-token implementation.
     */
    private long refreshExpiration = 604_800_000L;

    /**
     * Value prepended to the raw JWT in the Authorization header.
     * Default: {@code "Bearer "} (note the trailing space).
     */
    private String tokenPrefix = "Bearer ";

    /**
     * HTTP request header that carries the JWT.
     * Default: {@code "Authorization"}.
     */
    private String headerName = "Authorization";
}
