package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

public class LoginDto {

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    @NotNull(message = "Password must not be null")
    private String passwordEncoded;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordEncoded() {
        return passwordEncoded;
    }

    public void setPasswordEncoded(String passwordEncoded) {
        this.passwordEncoded = passwordEncoded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoginDto userLoginDto)) {
            return false;
        }
        return Objects.equals(email, userLoginDto.email)
            && Objects.equals(passwordEncoded, userLoginDto.passwordEncoded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, passwordEncoded);
    }

    @Override
    public String toString() {
        return "UserLoginDto{"
            + "email='" + email + '\''
            + ", passwordEncoded='" + passwordEncoded + '\''
            + '}';
    }


    public static final class UserLoginDtoBuilder {
        private String email;
        private String password;

        private UserLoginDtoBuilder() {
        }

        public static UserLoginDtoBuilder anUserLoginDto() {
            return new UserLoginDtoBuilder();
        }

        public UserLoginDtoBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserLoginDtoBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public LoginDto build() {
            LoginDto userLoginDto = new LoginDto();
            userLoginDto.setEmail(email);
            userLoginDto.setPasswordEncoded(password);
            return userLoginDto;
        }
    }
}
