package at.ac.tuwien.sepr.groupphase.backend.entity;

//TODO: replace this class with a correct ApplicationUser Entity implementation
public class ApplicationUser {

    private String email;
    private String password;
    private String nickname;
    private Boolean admin;

    public ApplicationUser() {
    }

    public ApplicationUser(String email, String password, Boolean admin) {
        this.email = email;
        this.password = password;
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }

    public ApplicationUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public ApplicationUser setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public ApplicationUser setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public ApplicationUser setAdmin(Boolean admin) {
        this.admin = admin;
        return this;
    }
}
