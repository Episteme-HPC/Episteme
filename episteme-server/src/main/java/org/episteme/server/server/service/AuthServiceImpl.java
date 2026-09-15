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

package org.episteme.server.server.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.episteme.server.server.auth.JWTUtil;
import org.episteme.server.server.auth.OIDCProvider;
import org.episteme.server.server.auth.Roles;
import org.episteme.server.server.model.User;
import org.episteme.server.server.proto.*;
import org.episteme.server.server.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * gRPC Service implementation for user authentication.
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */
@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private static final Logger LOG = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final UserRepository userRepository;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<AuthResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();
        String role = request.getRole();
        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Username already exists")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        // Enforce safe default role: never allow self-registering as ADMIN or ROOT
        String assignedRole = Roles.SCIENTIST;
        if (Roles.VIEWER.equalsIgnoreCase(role)) {
            assignedRole = Roles.VIEWER;
        }

        // Hash password
        String hashedPassword = hashPassword(password);
        User newUser = new User(username, hashedPassword, assignedRole);
        userRepository.save(newUser);
        LOG.info("Registered new user: {} with role: {}", username, assignedRole);

        String token = JWTUtil.generateToken(username, assignedRole);
        responseObserver.onNext(AuthResponse.newBuilder()
                .setSuccess(true)
                .setToken(token)
                .setMessage("Registration successful")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<AuthResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();

        // OIDC Logic preserved
        if (username.startsWith("oidc:")) {
            String provider = username.substring(5);
            OIDCProvider.TokenInfo oidcUser = OIDCProvider.validateToken(provider, password);

            if (oidcUser != null) {
                if (userRepository.findByUsername(oidcUser.email()).isEmpty()) {
                    userRepository.save(new User(oidcUser.email(), "oidc-managed", oidcUser.role()));
                }
                String token = JWTUtil.generateToken(oidcUser.email(), oidcUser.role());
                LOG.info("OIDC Login successful for: {}", oidcUser.email());
                responseObserver.onNext(AuthResponse.newBuilder().setSuccess(true).setToken(token)
                        .setMessage("OIDC Login successful").build());
                responseObserver.onCompleted();
                return;
            } else {
                responseObserver
                        .onNext(AuthResponse.newBuilder().setSuccess(false).setMessage("Invalid OIDC Token").build());
                responseObserver.onCompleted();
                return;
            }
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Invalid username or password")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        User user = userOpt.get();
        // Compare Hashes
        if (!hashPassword(password).equals(user.getPasswordHash())) {
            responseObserver.onNext(AuthResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Invalid username or password")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        String token = JWTUtil.generateToken(username, user.getRole());
        LOG.info("User logged in: {}", username);
        responseObserver.onNext(AuthResponse.newBuilder()
                .setSuccess(true)
                .setToken(token)
                .setMessage("Login successful")
                .build());
        responseObserver.onCompleted();
    }

    private String hashPassword(String plain) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Override
    public void validateToken(TokenRequest request, StreamObserver<ValidationResponse> responseObserver) {
        String token = request.getToken();
        String username = JWTUtil.validateAndGetUsername(token);
        String role = JWTUtil.getRole(token);

        if (username != null && role != null) {
            responseObserver.onNext(ValidationResponse.newBuilder()
                    .setValid(true)
                    .setUsername(username)
                    .setRole(role)
                    .build());
        } else {
            responseObserver.onNext(ValidationResponse.newBuilder()
                    .setValid(false)
                    .build());
        }
        responseObserver.onCompleted();
    }
}
