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

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;

/**
 * JWT Token Utility for generating and validating tokens.
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
public class JWTUtil {
    private static volatile SecretKey secretKey = initSecretKey();
    private static final long EXPIRATION_MS = 3600000; // 1 hour

    private static SecretKey initSecretKey() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = System.getProperty("security.jwt.secret");
        }
        if (secret == null || secret.isBlank()) {
            try {
                secret = org.episteme.core.io.Configuration.get("security.jwt.secret");
            } catch (Throwable ignored) {
            }
        }
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            // Default 256-bit key for dev/fallback if none configured
            secret = "Episteme-Production-Secure-256Bit-JWT-Key-2026-Strict!";
        }
        return Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public static void setSecret(String secret) {
        if (secret != null && secret.length() >= 32) {
            secretKey = Keys.hmacShaKeyFor(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    public static String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    public static String validateAndGetUsername(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getRole(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }
}
