package com.bodha.service;

import com.bodha.dto.LearnerProfileResponseDto;
import com.bodha.dto.UpdateProfileRequestDto;
import com.bodha.exception.ResourceNotFoundException;
import com.bodha.model.LearnerProfile;
import com.bodha.model.User;
import com.bodha.repository.LearnerProfileRepository;
import com.bodha.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing persistent learner profile metrics, bio, avatar, and study stats (Module A).
 */
@Service
public class LearnerProfileService {

    private final LearnerProfileRepository learnerProfileRepository;
    private final UserRepository userRepository;

    public LearnerProfileService(LearnerProfileRepository learnerProfileRepository,
                                 UserRepository userRepository) {
        this.learnerProfileRepository = learnerProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the profile associated with a specific user.
     *
     * @param userId user identifier
     * @return the learner profile DTO
     * @throws ResourceNotFoundException if user is not found
     */
    @Transactional(readOnly = true)
    public LearnerProfileResponseDto getProfileByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        LearnerProfile profile = learnerProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    LearnerProfile newProfile = new LearnerProfile(user);
                    return learnerProfileRepository.save(newProfile);
                });

        return LearnerProfileResponseDto.fromEntity(profile);
    }

    /**
     * Updates an existing learner profile and user name.
     *
     * @param userId identifier of the user whose profile is to be updated
     * @param request update payload with fullName, bio, avatarUrl
     * @return updated profile response DTO
     * @throws ResourceNotFoundException if the user does not exist
     */
    @Transactional
    public LearnerProfileResponseDto updateProfile(Long userId, UpdateProfileRequestDto request) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        LearnerProfile profile = learnerProfileRepository.findByUserId(userId)
                .orElseGet(() -> new LearnerProfile(user));

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
            userRepository.save(user);
        }

        if (request.bio() != null) {
            profile.setBio(request.bio().trim());
        }

        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl().trim());
        }

        LearnerProfile updated = learnerProfileRepository.save(profile);
        return LearnerProfileResponseDto.fromEntity(updated);
    }
}
