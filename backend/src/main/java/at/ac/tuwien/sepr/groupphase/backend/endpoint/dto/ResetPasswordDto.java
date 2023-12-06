package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class ResetPasswordDto {

    @NotNull(message = "Reset ID must not be null")
    private String resetId;

    @NotNull(message = "New password must not be null")
    private String newPassword;

    public String getResetId() {
        return resetId;
    }

    public void setResetId(String resetId) {
        this.resetId = resetId;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResetPasswordDto that = (ResetPasswordDto) o;
        return Objects.equals(resetId, that.resetId) && Objects.equals(newPassword, that.newPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resetId, newPassword);
    }

    @Override
    public String toString() {
        return "ResetPasswordDto{" +
            "resetId='" + resetId + '\'' +
            ", newPassword='" + newPassword + '\'' +
            '}';
    }
}
