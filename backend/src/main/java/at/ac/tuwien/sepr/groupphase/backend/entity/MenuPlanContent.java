package at.ac.tuwien.sepr.groupphase.backend.entity;

import at.ac.tuwien.sepr.groupphase.backend.entity.idclasses.MenuPlanContentId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;

@Entity
@IdClass(MenuPlanContentId.class)
public class MenuPlanContent {

    @Id
    @Column(nullable = false)
    private Integer timeslot;

    @Id
    @Column(nullable = false)
    private Integer dayIdx;

    @Id
    @ManyToOne
    @JoinColumn(name = "menuplan", nullable = false)
    private MenuPlan menuplan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn()
    private Recipe recipe;

    /* CONSTRUCTORS */

    public MenuPlanContent() {
        this(null, null, null, null);
    }

    public MenuPlanContent(MenuPlan menuPlan, Integer day, Integer timeslot) {
        this(menuPlan, day, timeslot, null);
    }

    public MenuPlanContent(MenuPlan menuPlan, Integer day, Integer timeslot, Recipe recipe) {
        this.menuplan = menuPlan;
        this.dayIdx = day;
        this.timeslot = timeslot;
        this.recipe = recipe;
    }

    /* GETTERS and SETTERS */

    public Integer getTimeslot() {
        return timeslot;
    }

    public MenuPlanContent setTimeslot(Integer timeslot) {
        this.timeslot = timeslot;
        return this;
    }

    public Integer getDayIdx() {
        return dayIdx;
    }

    public MenuPlanContent setDayIdx(Integer day) {
        this.dayIdx = day;
        return this;
    }

    public MenuPlan getMenuplan() {
        return menuplan;
    }

    public MenuPlanContent setMenuplan(MenuPlan menuplan) {
        this.menuplan = menuplan;
        return this;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public MenuPlanContent setRecipe(Recipe recipe) {
        this.recipe = recipe;
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
        MenuPlanContent that = (MenuPlanContent) o;
        return Objects.equals(timeslot, that.timeslot) && Objects.equals(dayIdx, that.dayIdx) && Objects.equals(menuplan, that.menuplan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeslot, dayIdx, menuplan);
    }
}


