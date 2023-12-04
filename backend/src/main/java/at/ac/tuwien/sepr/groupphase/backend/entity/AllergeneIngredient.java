package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.Objects;

@Entity
public class AllergeneIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "allergene_id")
    private Allergene allergene;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setAllergene(Allergene allergene) {
        this.allergene = allergene;
    }

    public Allergene getAllergene() {
        return allergene;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AllergeneIngredient allergeneIngredient)) {
            return false;
        }
        return Objects.equals(id, allergeneIngredient.id)
            && Objects.equals(allergene, allergeneIngredient.allergene)
            && Objects.equals(ingredient, allergeneIngredient.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, allergene, ingredient);
    }

    @Override
    public String toString() {
        return "AllergeneIngredient{"
            + "id=" + id
            + "allergene=" + allergene
            + "ingredient=" + ingredient
            + '}';
    }
}
