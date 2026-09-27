package com.bodha.model;

/**
 * Enum representing the three access roles defined in the `users` table.
 *
 * Stored as a VARCHAR(30) string in PostgreSQL (not as an ordinal integer)
 * via @Enumerated(EnumType.STRING) in User.java.
 *
 * Schema: CHECK (role IN ('LEARNER', 'MENTOR', 'ADMIN'))
 */
public enum UserRole {
    LEARNER,
    MENTOR,
    ADMIN
}
