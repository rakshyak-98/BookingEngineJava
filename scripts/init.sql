-- Create database if not exists
CREATE DATABASE IF NOT EXISTS booking_engine_dev;
USE booking_engine_dev;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'CUSTOMER', 'VENDOR', 'SUPPORT') DEFAULT 'CUSTOMER',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    service_type VARCHAR(100) NOT NULL,
    booking_date TIMESTAMP NOT NULL,
    start_date_time TIMESTAMP NOT NULL,
    end_date_time TIMESTAMP NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'RESCHEDULED') DEFAULT 'PENDING',
    total_amount DECIMAL(10, 2) NOT NULL,
    paid_amount DECIMAL(10, 2) DEFAULT 0,
    payment_status ENUM('PENDING', 'PARTIAL', 'COMPLETED', 'REFUNDED', 'FAILED'),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_booking_number (booking_number),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create payment transactions table
CREATE TABLE IF NOT EXISTS payment_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    transaction_id VARCHAR(100) UNIQUE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50),
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    gateway_response LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    INDEX idx_booking_id (booking_id),
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create audit logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    action VARCHAR(50),
    old_value LONGTEXT,
    new_value LONGTEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data
INSERT INTO users (email, password, first_name, last_name, phone, role, active)
VALUES
    ('admin@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoM2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'Admin', 'User', '+1234567890', 'ADMIN', TRUE),
    ('customer@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoM2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'John', 'Doe', '+1234567891', 'CUSTOMER', TRUE),
    ('vendor@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoM2OPST9/PgBkqquzi.Ss7KIUgO2t0jWMUW', 'Jane', 'Smith', '+1234567892', 'VENDOR', TRUE);

-- Create index for better performance
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_booking_status_user ON bookings(status, user_id);
CREATE INDEX idx_booking_date_range ON bookings(start_date_time, end_date_time);
