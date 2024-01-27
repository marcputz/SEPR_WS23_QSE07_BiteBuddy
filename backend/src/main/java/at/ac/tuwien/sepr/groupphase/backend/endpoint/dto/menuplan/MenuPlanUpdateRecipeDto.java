package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan;

public class MenuPlanUpdateRecipeDto {
    int menuPlanId;
    int day;
    int timeslot;
    boolean dislike;

    public int getMenuPlanId() {
        return menuPlanId;
    }

    // Setter for menuPlanId
    public void setMenuPlanId(int menuPlanId) {
        this.menuPlanId = menuPlanId;
    }

    // Getter for day
    public int getDay() {
        return day;
    }

    // Setter for day
    public void setDay(int day) {
        this.day = day;
    }

    // Getter for timeslot
    public int getTimeslot() {
        return timeslot;
    }

    // Setter for timeslot
    public void setTimeslot(int timeslot) {
        this.timeslot = timeslot;
    }

    public boolean isDislike() {
        return dislike;
    }

    public void setDislike(boolean dislike) {
        this.dislike = dislike;
    }
}

