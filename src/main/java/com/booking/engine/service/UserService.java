package com.booking.engine.service;

import com.booking.engine.dto.UserDTO;
import com.booking.engine.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);

    UserDTO getUserById(Long id);

    UserDTO getUserByEmail(String email);

    Page<UserDTO> getAllUsers(Pageable pageable);

    Page<UserDTO> getUsersByRole(User.UserRole role, Pageable pageable);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    boolean existsByEmail(String email);
}
