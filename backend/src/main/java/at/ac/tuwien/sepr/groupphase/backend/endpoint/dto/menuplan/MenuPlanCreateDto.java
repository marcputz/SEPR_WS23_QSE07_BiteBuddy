package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * DTO class for client to send menu plan creation data to this server.
 *
 * @author Marc Putz
 */
public class MenuPlanCreateDto {

    @NotNull(message = "Profile ID must not be null")
    private Long profileId;

    @NotNull(message = "From Time must not be null")
    private LocalDate fromTime;

    @NotNull(message = "Until Time must not be null")
    private LocalDate untilTime;

    private List<String> fridge;

    /* GETTER and SETTER */

    public Long getProfileId() {
        return profileId;
    }

    public MenuPlanCreateDto setProfileId(Long profileId) {
        this.profileId = profileId;
        return this;
    }

    public LocalDate getFromTime() {
        return fromTime;
    }

    public MenuPlanCreateDto setFromTime(LocalDate fromTime) {
        this.fromTime = fromTime;
        return this;
    }

    public LocalDate getUntilTime() {
        return untilTime;
    }

    public MenuPlanCreateDto setUntilTime(LocalDate untilTime) {
        this.untilTime = untilTime;
        return this;
    }

    public MenuPlanCreateDto setFridge(List<String> fridge) {
        this.fridge = fridge;
        return this;
    }

    public List<String> getFridge() {
        return this.fridge;
    }

    /* EQUALS and HASHCODE */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MenuPlanCreateDto that = (MenuPlanCreateDto) o;
        return Objects.equals(profileId, that.profileId) && Objects.equals(fromTime, that.fromTime) && Objects.equals(untilTime, that.untilTime) && Objects.equals(fridge, that.fridge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(profileId, fromTime, untilTime, fridge);
    }

    /* TO_STRING */

    @Override
    public String toString() {
        return "MenuPlanCreateDto{"
            + "profileId=" + profileId
            + ", fromTime=" + fromTime
            + ", untilTime=" + untilTime
            + ", fridge=" + fridge
            + '}';
    }
}
