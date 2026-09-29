package vn.iotstar.models;

import lombok.*;

/**
 * DTO for user registration request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserModel {

    private String fullName;
    private String email;
    private String password;
}
