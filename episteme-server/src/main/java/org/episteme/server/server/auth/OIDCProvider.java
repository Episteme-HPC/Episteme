/*
 * Episteme - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2025-2026 - Silvere Martin-Michiellot and Gemini AI (Google DeepMind)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.episteme.server.server.auth;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Production OIDC (OpenID Connect) Provider for Enterprise Authentication.
 * 
 * Supports multiple identity providers:
 * - Google OAuth 2.0 / OIDC
 * - Keycloak
 * - Okta
 * - Azure AD
 * 
 * Uses JWT validation with public key verification against provider's JWKS
 * endpoint.
 * 
 * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html">OIDC
 *      Specification</a>
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class OIDCProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCProvider.class);

    // Cache for JWT Processors (Duration: 1h to allow key rotation)
    private static final Cache<String, ConfigurableJWTProcessor<SecurityContext>> processorCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    // JWKS URIs loaded from configuration
    private static String getProviderJwksUrl(String provider) {
        return org.episteme.core.io.Configuration.get("auth.oidc.provider." + provider.toLowerCase() + ".jwks");
    }

    /**
     * Validates an ID token from an OIDC provider.
     * 
     * @param provider Provider name (google, keycloak, okta, azure)
     * @param token    ID token (JWT)
     * @return TokenInfo if valid, null otherwise
     */
    public static TokenInfo validateToken(String provider, String token) {
        if (token == null || token.isEmpty()) {
            LOG.warn("Empty token provided");
            return null;
        }

        try {
            ConfigurableJWTProcessor<SecurityContext> processor = getProcessor(provider);
            if (processor == null) {
                // This can happen if createProcessor failed due to MalformedURLException
                return null;
            }
            JWTClaimsSet claims = processor.process(token, null);

            // Extract standard OIDC claims
            String sub = claims.getSubject();
            String email = (String) claims.getClaim("email");

            // Extract Roles
            String role = Roles.VIEWER; // Default
            boolean isAdmin = false;

            // Provider-specific role mapping
            if ("google".equalsIgnoreCase(provider)) {
                role = Roles.SCIENTIST;
            } else if ("keycloak".equalsIgnoreCase(provider)) {
                Map<String, Object> realmAccess = claims.getJSONObjectClaim("realm_access");
                if (realmAccess != null && realmAccess.containsKey("roles")) {
                    String rolesStr = realmAccess.get("roles").toString();
                    if (rolesStr.contains("ADMIN")) {
                        role = Roles.ADMIN;
                        isAdmin = true;
                    } else if (rolesStr.contains("SCIENTIST")) {
                        role = Roles.SCIENTIST;
                    }
                }
            } else if ("okta".equalsIgnoreCase(provider)) {
                Object groups = claims.getClaim("groups");
                if (groups != null) {
                    String groupsStr = groups.toString();
                    if (groupsStr.contains("admin-group")) {
                        role = Roles.ADMIN;
                        isAdmin = true;
                    } else if (groupsStr.contains("scientist-group")) {
                        role = Roles.SCIENTIST;
                    }
                }
            }

            return new TokenInfo(sub, email, provider, role, isAdmin);

        } catch (ParseException | BadJOSEException | JOSEException e) {
            LOG.warn("Token validation failed for provider {}: {}", provider, e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            LOG.warn(e.getMessage());
            return null;
        }
    }

    private static ConfigurableJWTProcessor<SecurityContext> getProcessor(String provider) {
        return processorCache.get(provider, key -> {
            try {
                return createProcessor(key);
            } catch (MalformedURLException e) {
                LOG.error("Invalid JWKS URL for provider {}", key, e);
                return null;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static ConfigurableJWTProcessor<SecurityContext> createProcessor(String provider)
            throws MalformedURLException {
        String jwksUrl = getProviderJwksUrl(provider);
        if (jwksUrl == null) {
            throw new IllegalArgumentException("Unknown provider: " + provider);
        }

        ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
        JWKSource<SecurityContext> keySource = null;
        try {
            keySource = new RemoteJWKSet<>(URI.create(jwksUrl).toURL());
        } catch (MalformedURLException e) {
            // Should be caught by URI validation but stricter handling here
            throw e;
        }
        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
        processor.setJWSKeySelector(keySelector);
        return processor;
    }

    /**
     * For testing purposes only: allows injecting a custom processor (e.g., with
     * local keys).
     */
    public static void setJwtProcessorForTest(String provider, ConfigurableJWTProcessor<SecurityContext> processor) {
        processorCache.put(provider, processor);
    }

    // TokenInfo Record
    public record TokenInfo(String sub, String email, String provider, String role, boolean isAdmin) {
    }
}

