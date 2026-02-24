package com.howmuch.service;

import com.howmuch.domain.KycStatus;
import com.howmuch.domain.Role;
import com.howmuch.domain.User;
import com.howmuch.dto.AuthRequest;
import com.howmuch.dto.AuthResponse;
import com.howmuch.dto.RegisterRequest;
import com.howmuch.dto.UserResponse;
import com.howmuch.exception.ApiException;
import com.howmuch.repository.UserRepository;
import com.howmuch.security.JwtService;
import com.howmuch.wallet.Wallet;
import com.howmuch.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final WalletRepository walletRepository;

    public AuthService(UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already in use");
        }

        Role role = request.role();
        if (role == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Admin self-registration is not allowed");
        }

        User user = new User(
                request.email().toLowerCase(),
                request.password(),
                role,
                KycStatus.PENDING
        );

        User saved = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(saved);
        walletRepository.save(wallet);

        return new UserResponse(saved.getId(), saved.getEmail(), saved.getRole(), saved.getKycStatus());
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password())
        );

        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build(),
                Map.of("role", user.getRole().name(), "kycStatus", user.getKycStatus().name())
        );

        return new AuthResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }
}
