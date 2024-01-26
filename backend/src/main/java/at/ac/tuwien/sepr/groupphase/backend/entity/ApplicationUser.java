package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "users")
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String passwordEncoded;

    @Column(nullable = false, unique = true, length = 255)
    private String nickname;

    @Lob
    @Column
    private byte[] userPicture;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @OneToOne
    private Profile activeProfile;

    public ApplicationUser() {
    }

    public ApplicationUser(String email, String passwordEncoded) {
        this.email = email;
        this.passwordEncoded = passwordEncoded;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ApplicationUser setId(Long id) {
        this.id = id;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ApplicationUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPasswordEncoded() {
        return passwordEncoded;
    }

    public ApplicationUser setPasswordEncoded(String password) {
        this.passwordEncoded = password;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public ApplicationUser setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public Profile getActiveProfile() {
        return activeProfile;
    }

    public ApplicationUser setActiveProfile(Profile activeProfile) {
        this.activeProfile = activeProfile;
        return this;
    }

    public byte[] getUserPicture() {
        return userPicture;
    }

    public ApplicationUser setUserPicture(byte[] picture) {
        this.userPicture = picture;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /* EQUALS, HASHCODE, TOSTRING */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ApplicationUser that = (ApplicationUser) o;
        return id.equals(that.id) && email.equals(that.email) && passwordEncoded.equals(that.passwordEncoded)
            && nickname.equals(that.nickname) && Arrays.equals(userPicture, that.userPicture) && createdAt.equals(that.createdAt) && updatedAt.equals(that.updatedAt) && Objects.equals(activeProfile, that.activeProfile);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, email, passwordEncoded, nickname, createdAt, updatedAt, activeProfile);
        result = 31 * result + Arrays.hashCode(userPicture);
        return result;
    }

    @Override
    public String toString() {
        return "ApplicationUser{"
            + "id=" + id
            + ", email='" + email + '\''
            + ", passwordEncoded='" + passwordEncoded + '\''
            + ", nickname='" + nickname + '\''
            + ", userPicture=" + Arrays.toString(userPicture)
            + ", createdAt=" + createdAt
            + ", updatedAt=" + updatedAt
            + ", activeProfile=" + activeProfile
            + '}';
    }
}
