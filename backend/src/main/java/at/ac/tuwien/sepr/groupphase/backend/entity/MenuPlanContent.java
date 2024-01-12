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
    @ManyToOne
    @JoinColumn(name = "menuplan", nullable = false)
    private MenuPlan menuplan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn()
    private Recipe recipe;

    /* CONSTRUCTORS */

    public MenuPlanContent() {
        this(null, null, null);
    }

    public MenuPlanContent(MenuPlan menuPlan, Integer timeslot) {
        this(menuPlan, timeslot, null);
    }

    public MenuPlanContent(MenuPlan menuPlan, Integer timeslot, Recipe recipe) {
        this.menuplan = menuPlan;
        this.timeslot = timeslot;
        this.recipe = recipe;
    }

    /* GETTERS and SETTERS */

    public Integer getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Integer timeslot) {
        this.timeslot = timeslot;
    }

    public MenuPlan getMenuplan() {
        return menuplan;
    }

    public void setMenuplan(MenuPlan menuplan) {
        this.menuplan = menuplan;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    /* EQUALS and HASHCODE */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuPlanContent that = (MenuPlanContent) o;
        return Objects.equals(timeslot, that.timeslot) && Objects.equals(menuplan, that.menuplan);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeslot, menuplan);
    }
}


