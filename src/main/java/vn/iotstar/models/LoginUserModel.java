package vn.iotstar.models;

import lombok.*;

/**
 * DTO for user login request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginUserModel {

    private String email;
    private String password;
}
