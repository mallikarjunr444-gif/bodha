package com.bodha.service;

import com.bodha.dto.AuthResponseDto;
import com.bodha.dto.LoginRequestDto;
import com.bodha.dto.RegisterRequestDto;
import com.bodha.exception.DuplicateEmailException;
import com.bodha.exception.InvalidCredentialsException;
import com.bodha.model.LearnerProfile;
import com.bodha.model.User;
import com.bodha.model.UserRole;
import com.bodha.repository.LearnerProfileRepository;
import com.bodha.repository.UserRepository;
import com.bodha.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user authentication, credential verification, and account onboarding (Module A & N).
 * Issues cryptographically signed JWT tokens upon successful authentication.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       LearnerProfileRepository learnerProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.learnerProfileRepository = learnerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new learner account with hashed credentials, creates an initial profile,
     * and returns a signed JWT authentication token.
     *
     * @param request registration request containing email, password, and full name
     * @return safe authenticated user response with JWT token
     * @throws DuplicateEmailException if the email is already in use
     */
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("An account with email '" + normalizedEmail + "' already exists");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(normalizedEmail, passwordHash, request.fullName().trim(), UserRole.LEARNER);
        User savedUser = userRepository.save(user);

        LearnerProfile profile = new LearnerProfile(savedUser);
        learnerProfileRepository.save(profile);

        String token = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole() != null ? savedUser.getRole().name() : "LEARNER");

        return AuthResponseDto.fromUser(savedUser, token, "Registration successful");
    }

    /**
     * Authenticates a user against their stored BCrypt password hash and returns a signed JWT.
     *
     * @param request login credentials (email and password)
     * @return safe authenticated user response with JWT token
     * @throws InvalidCredentialsException if credentials do not match
     */
    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : "LEARNER");

        return AuthResponseDto.fromUser(user, token, "Login successful");
    }
}
