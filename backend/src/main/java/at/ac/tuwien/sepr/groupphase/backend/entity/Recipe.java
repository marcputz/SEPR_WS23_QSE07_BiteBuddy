package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Set;

@Entity
public class Recipe {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String instructions;
    @OneToMany(mappedBy = "recipe")
    private Set<RecipeIngredient> recipeIngredients;

    public Recipe setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Recipe setName(String name) {
        this.name = name;
        return this;
    }

    public String getInstructions() {
        return instructions;
    }

    public Recipe setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }

    public Set<RecipeIngredient> getIngredients() {
        return recipeIngredients;
    }

    public Recipe setIngredients(Set<RecipeIngredient> ingredients) {
        this.recipeIngredients = ingredients;
        return this;
    }
}