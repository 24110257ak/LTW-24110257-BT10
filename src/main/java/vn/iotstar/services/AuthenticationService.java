package vn.iotstar.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.iotstar.entity.User;
import vn.iotstar.models.LoginUserModel;
import vn.iotstar.models.RegisterUserModel;
import vn.iotstar.repository.UserRepository;

/**
 * AuthenticationService - Handles user signup and login business logic
 */
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user.
     * Password is encoded with BCrypt before saving.
     *
     * @param input the registration form data
     * @return the saved User entity
     */
    public User signup(RegisterUserModel input) {
        User user = User.builder()
                .fullName(input.getFullName())
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword())) // BCrypt encode
                .build();

        return userRepository.save(user);
    }

    /**
     * Authenticate an existing user.
     * Uses Spring Security's AuthenticationManager to validate credentials.
     *
     * @param input the login form data
     * @return the authenticated User entity
     */
    public User authenticate(LoginUserModel input) {
        // Throws an exception if authentication fails (bad credentials, user not found, etc.)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        // If we reach here, authentication was successful — fetch and return the user
        return userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));
    }
}
