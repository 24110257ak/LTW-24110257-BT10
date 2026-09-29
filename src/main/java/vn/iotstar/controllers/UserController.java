package vn.iotstar.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.iotstar.entity.User;
import vn.iotstar.services.UserService;

import java.util.List;

/**
 * UserController - REST API endpoints for user data (protected).
 *
 * Base path: /users
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /users/me
     * Returns the currently authenticated user's details.
     * Requires a valid JWT Bearer token in the Authorization header.
     *
     * Response: User object of the logged-in user
     */
    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return ResponseEntity.ok(currentUser);
    }

    /**
     * GET /users/
     * Returns a list of all registered users.
     * Requires a valid JWT Bearer token in the Authorization header.
     *
     * Response: List of User objects
     */
    @GetMapping("/")
    public ResponseEntity<List<User>> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }
}
