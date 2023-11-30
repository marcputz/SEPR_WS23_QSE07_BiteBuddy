package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDateTime;

@Entity
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
