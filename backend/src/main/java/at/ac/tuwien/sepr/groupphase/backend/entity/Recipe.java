package at.ac.tuwien.sepr.groupphase.backend.entity;


import jakarta.persistence.*;

import java.util.Objects;
import java.util.Set;


@Entity
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @Column
    private String instructions;
    @Lob
    @Column
    private byte[] picture;  // The image data represented as a byte array

    @OneToMany(mappedBy = "recipe")
    private Set<RecipeIngredient> recipeIngredients;


    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getInstructions() {
        return instructions;
    }

    public byte[] getPicture() {
        return picture;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public void setIngredients(Set<RecipeIngredient> ingredients) {
        this.recipeIngredients = ingredients;
    }

    public Set<RecipeIngredient> getIngredients() {
        return recipeIngredients;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Recipe recipe)) {
            return false;
        }
        return Objects.equals(id, recipe.id)
            && Objects.equals(name, recipe.name)
            && Objects.equals(instructions, recipe.instructions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, instructions);
    }

    @Override
    public String toString() {
        return "Recipe{"
            + "id=" + id
            + "name=" + name
            + "instructions=" + instructions
            + '}';
    }

}