package com.booking.engine.service.impl;

import com.booking.engine.dto.UserDTO;
import com.booking.engine.entity.User;
import com.booking.engine.exception.ResourceNotFoundException;
import com.booking.engine.repository.sql.UserRepository;
import com.booking.engine.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + userDTO.getEmail());
        }

        User user = User.builder()
            .email(userDTO.getEmail())
            .password(passwordEncoder.encode(userDTO.getEmail()))
            .firstName(userDTO.getFirstName())
            .lastName(userDTO.getLastName())
            .phone(userDTO.getPhone())
            .role(userDTO.getRole() != null ? userDTO.getRole() : User.UserRole.CUSTOMER)
            .active(true)
            .build();

        User savedUser = userRepository.save(user);
        log.info("User created: {}", savedUser.getEmail());
        return mapToDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> getUsersByRole(User.UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
            .map(this::mapToDTO);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated: {}", updatedUser.getEmail());
        return mapToDTO(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .role(user.getRole())
            .active(user.getActive())
            .createdAt(user.getCreatedAt().toString())
            .updatedAt(user.getUpdatedAt().toString())
            .build();
    }
}
