package com.booking.engine.controller;

import com.booking.engine.dto.UserDTO;
import com.booking.engine.entity.User;
import com.booking.engine.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> getUserById(
        @PathVariable
        @Parameter(description = "User ID") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieve a user by their email address")
    public ResponseEntity<UserDTO> getUserByEmail(
        @PathVariable
        @Parameter(description = "User email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users with pagination")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Retrieve users filtered by role")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
        @PathVariable
        @Parameter(description = "User role") User.UserRole role,
        @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(userService.getUsersByRole(role, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> updateUser(
        @PathVariable Long id,
        @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user from the system")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    public ResponseEntity<Void> deleteUser(
        @PathVariable
        @Parameter(description = "User ID") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
