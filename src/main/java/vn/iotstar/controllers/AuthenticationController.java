package vn.iotstar.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.User;
import vn.iotstar.models.LoginResponse;
import vn.iotstar.models.LoginUserModel;
import vn.iotstar.models.RegisterUserModel;
import vn.iotstar.services.AuthenticationService;
import vn.iotstar.services.JwtService;

/**
 * AuthenticationController - REST API endpoints for signup and login.
 *
 * Base path: /auth
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    /**
     * POST /auth/signup
     * Register a new user.
     *
     * Request body: { "fullName": "...", "email": "...", "password": "..." }
     * Response: saved User object (without password ideally, simplified here)
     */
    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserModel registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    /**
     * POST /auth/login
     * Authenticate user and return JWT token.
     *
     * Request body: { "email": "...", "password": "..." }
     * Response: { "token": "eyJ...", "expiresIn": 3600000 }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserModel loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity.ok(loginResponse);
    }
}
