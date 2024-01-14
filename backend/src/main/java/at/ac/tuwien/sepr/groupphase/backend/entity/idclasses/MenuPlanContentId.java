package at.ac.tuwien.sepr.groupphase.backend.entity.idclasses;

import at.ac.tuwien.sepr.groupphase.backend.entity.MenuPlan;
import java.io.Serializable;
import java.util.Objects;

public class MenuPlanContentId implements Serializable {
    private Integer timeslot;

    private Integer dayIdx;

    private MenuPlan menuplan;

    public MenuPlanContentId(Integer timeslot, Integer day, MenuPlan menuplan) {
        this.timeslot = timeslot;
        this.dayIdx = day;
        this.menuplan = menuplan;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MenuPlanContentId that = (MenuPlanContentId) o;
        return Objects.equals(timeslot, that.timeslot) && Objects.equals(menuplan, that.menuplan) && Objects.equals(dayIdx, that.dayIdx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeslot, dayIdx, menuplan);
    }
}
