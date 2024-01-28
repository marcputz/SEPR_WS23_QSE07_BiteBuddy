package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/**
 * Data object transfer for Profile entity to send information through the network and between layers.
 */

public class ProfileListDto {

    @JsonProperty("id")
    @NotNull(message = "ID cannot be NULL value")
    private Long id;

    @JsonProperty("name")
    @NotNull(message = "Name cannot be NULL value")
    private String name;

    @JsonProperty("userId")
    @NotNull(message = "User ID cannot be NULL value")
    private Long userId;

    public Long getId() {
        return id;
    }

    public ProfileListDto setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ProfileListDto setName(String name) {
        this.name = name;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public ProfileListDto setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProfileListDto that = (ProfileListDto) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, userId);
    }
}
