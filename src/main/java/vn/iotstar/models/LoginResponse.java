package vn.iotstar.models;

import lombok.*;

/**
 * DTO for login API response containing JWT token and expiry duration
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private long expiresIn;
}
