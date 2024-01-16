package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;


import java.util.Objects;

@Entity
public class RecipeIngredientDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = true)
    private Float amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private FoodUnit unit;
    @Column
    private String ingredient;
    @Column(nullable = true)
    private String describer;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return amount;
    }

    public void setUnit(FoodUnit unit) {
        this.unit = unit;
    }

    public FoodUnit getUnit() {
        return unit;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setDescriber(String describer) {
        this.describer = describer;
    }

    public String getDescriber() {
        return describer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipeIngredientDetails recipeIngredientDetails)) {
            return false;
        }
        return Objects.equals(id, recipeIngredientDetails.id)
            && Objects.equals(amount, recipeIngredientDetails.amount)
            && Objects.equals(unit, recipeIngredientDetails.unit)
            && Objects.equals(ingredient, recipeIngredientDetails.ingredient)
            && Objects.equals(describer, recipeIngredientDetails.describer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, unit, ingredient, describer);
    }

    @Override
    public String toString() {
        return "RecipeIngredientDetails{"
            + "id=" + id
            + "amount=" + amount
            + "unit=" + unit
            + "ingredient=" + ingredient
            + "describer=" + describer
            + '}';
    }
}
