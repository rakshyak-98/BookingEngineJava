package com.booking.engine.service;

import com.booking.engine.dto.UserDTO;
import com.booking.engine.entity.User;
import com.booking.engine.exception.ResourceNotFoundException;
import com.booking.engine.repository.sql.UserRepository;
import com.booking.engine.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO userDTO;
    private User user;

    @BeforeEach
    void setUp() {
        userDTO = UserDTO.builder()
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .phone("1234567890")
            .role(User.UserRole.CUSTOMER)
            .active(true)
            .build();

        user = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("encoded_password")
            .firstName("John")
            .lastName("Doe")
            .phone("1234567890")
            .role(User.UserRole.CUSTOMER)
            .active(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.createUser(userDTO);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testCreateUserWithDuplicateEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testGetUserByIdNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void testGetUserByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should check if email exists")
    void testExistsByEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        boolean exists = userService.existsByEmail("test@example.com");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void testDeleteUserNotFound() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999L));
    }
}
