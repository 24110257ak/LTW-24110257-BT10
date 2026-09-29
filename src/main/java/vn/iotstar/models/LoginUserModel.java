package vn.iotstar.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginUserModel {
    private String email;
    private String password;
}
