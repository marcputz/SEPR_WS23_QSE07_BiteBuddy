package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Set;

@Entity
public class Ingredient {
    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "ingredient")
    private Set<RecipeIngredient> recipeIngredients;

    public Long getId() {
        return id;
    }

    public Ingredient setId(Long id) {
        this.id = id;
        return this;
    }

    public Set<RecipeIngredient> getRecipeIngredients() {
        return recipeIngredients;
    }

    public Ingredient setRecipeIngredients(Set<RecipeIngredient> recipeIngredients) {
        this.recipeIngredients = recipeIngredients;
        return this;
    }
}
