package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    private List<Profile> profiles = new ArrayList<>();


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

    /**
     * checks if a specified password matches the user's password.
     *
     * @param passwordEncoded the password to check against.
     * @return {@code true} if specified password matches user password.
     * @author Marc Putz
     */
    public boolean checkPasswordMatch(String passwordEncoded) {
        if (passwordEncoded == null) {
            return false;
        }
        return passwordEncoded.equals(this.passwordEncoded);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
