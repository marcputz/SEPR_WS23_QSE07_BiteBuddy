package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * DTO used for transmitting login data.
 */
public class LoginDto {

    @NotNull(message = "Email must not be null")
    @Email
    private String email;

    @NotNull(message = "Password must not be null")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
            && Objects.equals(password, userLoginDto.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return "UserLoginDto{"
            + "email='" + email + '\''
            + ", password='" + password + '\''
            + '}';
    }


    public static final class LoginDtobuilder {
        private String email;
        private String password;

        private LoginDtobuilder() {
        }

        public static LoginDtobuilder anLoginDto() {
            return new LoginDtobuilder();
        }

        public LoginDtobuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public LoginDtobuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public LoginDto build() {
            LoginDto userLoginDto = new LoginDto();
            userLoginDto.setEmail(email);
            userLoginDto.setPassword(password);
            return userLoginDto;
        }
    }
}
