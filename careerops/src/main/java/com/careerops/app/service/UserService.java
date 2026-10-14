package com.careerops.app.service;

import com.careerops.app.dto.auth.UserResponse;
import com.careerops.app.entity.User;
import com.careerops.app.exception.ResourceNotFoundException;
import com.careerops.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    public UserResponse findByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toResponse(user);
    }

    public User getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getEntityByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreatedAt());
    }
}
