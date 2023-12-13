package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class PasswordResetRequest {
    @Id
    private String id;
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationUser user;
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime requestTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public void setUser(ApplicationUser user) {
        this.user = user;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordResetRequest that = (PasswordResetRequest) o;
        return Objects.equals(id, that.id) && Objects.equals(user, that.user) && Objects.equals(requestTime, that.requestTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, requestTime);
    }

    @Override
    public String toString() {
        return "PasswordChangeRequest{" +
            "id=" + id +
            ", user=" + user +
            ", requestTime=" + requestTime +
            '}';
    }
}
