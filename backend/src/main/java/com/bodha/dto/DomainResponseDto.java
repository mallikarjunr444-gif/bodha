package com.bodha.dto;

import com.bodha.model.Domain;

/**
 * Data Transfer Object representing a knowledge domain (Module B).
 */
public record DomainResponseDto(
    String id,
    String name,
    String description
) {
    public static DomainResponseDto fromEntity(Domain domain) {
        if (domain == null) return null;
        return new DomainResponseDto(
            domain.getId(),
            domain.getName(),
            domain.getDescription()
        );
    }
}
