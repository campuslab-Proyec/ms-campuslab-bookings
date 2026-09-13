CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id VARCHAR(50) NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    booking_from DATETIME NOT NULL,
    booking_to DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_resource ON bookings(resource_id);