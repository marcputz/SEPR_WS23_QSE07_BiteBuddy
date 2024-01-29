package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.menuplan;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeListDto;

import java.util.Objects;

public class MenuPlanContentDetailDto {
    private String creator;
    private int day;
    private int timeslot;
    private RecipeListDto recipe;

    /* GETTERS and SETTERS */

    public String getCreator() {
        return creator;
    }

    public MenuPlanContentDetailDto setCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public int getDay() {
        return day;
    }

    public MenuPlanContentDetailDto setDay(int day) {
        this.day = day;
        return this;
    }

    public int getTimeslot() {
        return timeslot;
    }

    public MenuPlanContentDetailDto setTimeslot(int timeslot) {
        this.timeslot = timeslot;
        return this;
    }

    public RecipeListDto getRecipe() {
        return recipe;
    }

    public MenuPlanContentDetailDto setRecipe(RecipeListDto recipe) {
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
        MenuPlanContentDetailDto that = (MenuPlanContentDetailDto) o;
        return day == that.day && timeslot == that.timeslot && Objects.equals(recipe, that.recipe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, timeslot, recipe);
    }
}
