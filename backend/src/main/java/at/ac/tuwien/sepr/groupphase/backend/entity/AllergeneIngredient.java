package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

    public Long getId() {
        return id;
    }

    public AllergeneIngredient setId(Long id) {
        this.id = id;
        return this;
    }

    public Allergene getAllergene() {
        return allergene;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public AllergeneIngredient setAllergene(Allergene allergene) {
        this.allergene = allergene;
        return this;
    }

    public AllergeneIngredient setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
        return this;
    }
}
