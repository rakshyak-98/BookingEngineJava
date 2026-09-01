package com.booking.engine.repository.sql;

import com.booking.engine.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Page<User> findByRole(User.UserRole role, Pageable pageable);

    Page<User> findByActive(Boolean active, Pageable pageable);

    boolean existsByEmail(String email);
}
