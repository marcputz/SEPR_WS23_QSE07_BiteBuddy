package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class UserUpdateEmailAndPasswordDto {
    String email;
    String currentPassword;
    String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public String getEmail() {
        return email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public String toString() {
        return "UserUpdateDto{"
            + "email='" + email + '\''
            + '}';
    }

    public static final class UserUpdateDtoBuilder {
        private String email;
        private String currentPassword;
        private String newPassword;

        private UserUpdateDtoBuilder() {
        }

        public static UserUpdateDtoBuilder anUserUpdateDto() {
            return new UserUpdateDtoBuilder();
        }

        public UserUpdateDtoBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserUpdateDtoBuilder withCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
            return this;
        }

        public UserUpdateDtoBuilder withNewPassword(String password) {
            this.newPassword = password;
            return this;
        }

        public UserUpdateEmailAndPasswordDto build() {
            UserUpdateEmailAndPasswordDto userUpdateEmailAndPasswordDto = new UserUpdateEmailAndPasswordDto();
            userUpdateEmailAndPasswordDto.setEmail(email);
            userUpdateEmailAndPasswordDto.setCurrentPassword(currentPassword);
            userUpdateEmailAndPasswordDto.setNewPassword(newPassword);
            return userUpdateEmailAndPasswordDto;
        }
    }
}
