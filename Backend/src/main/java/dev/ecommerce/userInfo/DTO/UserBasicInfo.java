package dev.ecommerce.userInfo.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserBasicInfo {
        private String firstname;
        private String lastname;
        private String email;
        private String password;
        private String newPass;
        private String confirmPass;
}
