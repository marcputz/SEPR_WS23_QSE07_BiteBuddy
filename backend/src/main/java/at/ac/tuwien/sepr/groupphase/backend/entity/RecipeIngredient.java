package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

    public RecipeIngredient setId(Long id) {
        this.id = id;
        return this;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public RecipeIngredient setRecipe(Recipe recipe) {
        this.recipe = recipe;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public RecipeIngredient setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public RecipeIngredient setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }
}
