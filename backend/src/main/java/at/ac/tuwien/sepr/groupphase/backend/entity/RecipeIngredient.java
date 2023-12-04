package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.Objects;

@Entity
public class RecipeIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;
    private String amount;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipeIngredient recipeIngredient)) {
            return false;
        }
        return Objects.equals(id, recipeIngredient.id)
            && Objects.equals(recipe, recipeIngredient.recipe)
            && Objects.equals(ingredient, recipeIngredient.ingredient)
            && Objects.equals(amount, recipeIngredient.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recipe, ingredient, amount);
    }

    @Override
    public String toString() {
        return "RecipeIngredient{"
            + "id=" + id
            + "recipe=" + recipe
            + "ingredient=" + ingredient
            + "amount=" + amount
            + '}';
    }
}
