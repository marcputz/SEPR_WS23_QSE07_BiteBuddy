package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class UserUpdateDto {
    String email;
    String nickname;
    String password;

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "UserUpdateDto{"
            + "email='" + email + '\''
            + ", name='" + nickname + '\''
            + '}';
    }

    public static final class UserUpdateDtoBuilder {
        private String email;
        private String nickname;
        private String password;

        private UserUpdateDtoBuilder() {
        }

        public static UserUpdateDtoBuilder anUserUpdateDto() {
            return new UserUpdateDtoBuilder();
        }

        public UserUpdateDtoBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserUpdateDtoBuilder withName(String name) {
            this.nickname = name;
            return this;
        }

        public UserUpdateDtoBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public UserUpdateDto build() {
            UserUpdateDto userUpdateDto = new UserUpdateDto();
            userUpdateDto.setEmail(email);
            userUpdateDto.setNickname(nickname);
            userUpdateDto.setPassword(password);
            return userUpdateDto;
        }
    }
}
