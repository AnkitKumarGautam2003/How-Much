package com.howmuch.service;

import com.howmuch.domain.KycStatus;
import com.howmuch.domain.User;
import com.howmuch.dto.UserResponse;
import com.howmuch.exception.ApiException;
import com.howmuch.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.getKycStatus()))
                .toList();
    }

    @Transactional
    public UserResponse updateKycStatus(UUID userId, KycStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        user.setKycStatus(status);
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getEmail(), saved.getRole(), saved.getKycStatus());
    }
}
