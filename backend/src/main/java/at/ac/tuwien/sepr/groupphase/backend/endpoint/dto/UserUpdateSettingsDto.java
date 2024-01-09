package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class UserUpdateSettingsDto {
    private String nickname;
    private byte[] userPicture;

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

    @Override
    public String toString() {
        String userPictureLength = (userPicture != null) ? String.valueOf(userPicture.length) : "null";
        return "UserUpdateSettingsDto{"
            + "nickname='" + nickname + '\''
            + ", userPicture.length=" + userPictureLength
            + '}';
    }
}
