package com.booking.engine.dto;

import com.booking.engine.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private User.UserRole role;
    private Boolean active;
    private String createdAt;
    private String updatedAt;
}
