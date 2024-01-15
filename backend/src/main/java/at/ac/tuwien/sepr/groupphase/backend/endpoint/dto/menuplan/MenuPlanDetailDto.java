package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan;

import java.time.LocalDate;

public class MenuPlanDetailDto {

    private Long userId;

    private Long profileId;

    private String profileName;

    private LocalDate fromTime;

    private LocalDate untilTime;

    private int numDays;

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

    /* EQUALS and HASHCODE */
    // TODO: Generate equals and hashcode

}
