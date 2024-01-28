package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDateTime;

public class UserSettingsDto {
    private Long id;
    private String email;
    private String nickname;
    private byte[] userPicture;
    private Long activeProfileId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public byte[] getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(byte[] userPicture) {
        this.userPicture = userPicture;
    }

    public Long getActiveProfileId() {
        return activeProfileId;
    }

    public void setActiveProfileId(Long activeProfileId) {
        this.activeProfileId = activeProfileId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        String userPictureLength = (userPicture != null) ? String.valueOf(userPicture.length) : "null";
        return "UserSettingsDto{"
            + "id=" + id
            + ", email='" + email + '\''
            + ", nickname='" + nickname + '\''
            + ", userPicture.length=" + userPictureLength
            + ", activeProfileId=" + activeProfileId
            + ", createdAt=" + createdAt
            + ", updatedAt=" + updatedAt
            + '}';
    }


    public static final class UserSettingsDtoBuilder {
        private Long id;
        private String email;
        private String nickname;
        private Long activeProfileId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private UserSettingsDtoBuilder() {
        }

        public static UserSettingsDtoBuilder anUserSettingsDto() {
            return new UserSettingsDtoBuilder();
        }

        public UserSettingsDtoBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public UserSettingsDtoBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserSettingsDtoBuilder withNickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public UserSettingsDtoBuilder withActiveProfileId(Long activeProfileId) {
            this.activeProfileId = activeProfileId;
            return this;
        }

        public UserSettingsDtoBuilder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserSettingsDtoBuilder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public UserSettingsDto build() {
            UserSettingsDto userSettingsDto = new UserSettingsDto();
            userSettingsDto.id = this.id;
            userSettingsDto.email = this.email;
            userSettingsDto.nickname = this.nickname;
            userSettingsDto.activeProfileId = this.activeProfileId;
            userSettingsDto.createdAt = this.createdAt;
            userSettingsDto.updatedAt = this.updatedAt;
            return userSettingsDto;
        }
    }
}
