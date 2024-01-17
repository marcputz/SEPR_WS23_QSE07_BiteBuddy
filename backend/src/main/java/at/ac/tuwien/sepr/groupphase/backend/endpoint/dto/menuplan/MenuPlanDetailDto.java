package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

public class MenuPlanDetailDto {

    private Long userId;

    private Long profileId;

    private String profileName;

    private LocalDate fromTime;

    private LocalDate untilTime;

    private int numDays;

    private Set<MenuPlanContentDetailDto> contents;

    /* GETTER and SETTER */

    public Long getUserId() {
        return userId;
    }

    public MenuPlanDetailDto setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Long getProfileId() {
        return profileId;
    }

    public MenuPlanDetailDto setProfileId(Long profileId) {
        this.profileId = profileId;
        return this;
    }

    public String getProfileName() {
        return profileName;
    }

    public MenuPlanDetailDto setProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }

    public LocalDate getFromTime() {
        return fromTime;
    }

    public MenuPlanDetailDto setFromTime(LocalDate fromTime) {
        this.fromTime = fromTime;
        return this;
    }

    public LocalDate getUntilTime() {
        return untilTime;
    }

    public MenuPlanDetailDto setUntilTime(LocalDate untilTime) {
        this.untilTime = untilTime;
        return this;
    }

    public int getNumDays() {
        return numDays;
    }

    public MenuPlanDetailDto setNumDays(int numDays) {
        this.numDays = numDays;
        return this;
    }

    public Set<MenuPlanContentDetailDto> getContents() {
        return contents;
    }

    public MenuPlanDetailDto setContents(Set<MenuPlanContentDetailDto> contents) {
        this.contents = contents;
        return this;
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
        MenuPlanDetailDto that = (MenuPlanDetailDto) o;
        return numDays == that.numDays
            && Objects.equals(userId, that.userId)
            && Objects.equals(profileId, that.profileId)
            && Objects.equals(profileName, that.profileName)
            && Objects.equals(fromTime, that.fromTime)
            && Objects.equals(untilTime, that.untilTime)
            && Objects.equals(contents, that.contents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, profileId, profileName, fromTime, untilTime, numDays, contents);
    }

    /* TO_STRING */

    @Override
    public String toString() {
        return "MenuPlanDetailDto{"
            + "userId=" + userId
            + ", profileId=" + profileId
            + ", profileName='" + profileName + '\''
            + ", fromTime=" + fromTime
            + ", untilTime=" + untilTime
            + ", numDays=" + numDays
            + ", contents=" + contents
            + '}';
    }
}
