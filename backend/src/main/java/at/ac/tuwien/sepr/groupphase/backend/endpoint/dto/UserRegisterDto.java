package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class UserRegisterDto {
    String email;
    String name;
    String passwordEncoded;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordEncoded() {
        return passwordEncoded;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPasswordEncoded(String passwordEncoded) {
        this.passwordEncoded = passwordEncoded;
    }
}
