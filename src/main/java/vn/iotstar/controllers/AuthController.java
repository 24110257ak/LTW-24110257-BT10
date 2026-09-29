package vn.iotstar.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * AuthController - Thymeleaf view controller for HTML pages.
 * Renders login and profile pages (Thymeleaf templates).
 */
@Controller
public class AuthController {

    /**
     * GET /login
     * Renders the login page (Thymeleaf template: login.html)
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * GET /user/profile
     * Renders the user profile page (Thymeleaf template: profile.html)
     * The actual user data is loaded via AJAX/JavaScript from /users/me
     */
    @GetMapping("/user/profile")
    public String profilePage() {
        return "profile";
    }
}
