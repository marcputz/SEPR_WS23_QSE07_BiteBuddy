package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.OneToMany;

import java.util.Set;

@Entity
public class Allergene {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @OneToMany(mappedBy = "allergene")
    private Set<AllergeneIngredient> allergeneIngredients;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Allergene setName(String name) {
        this.name = name;
        return this;
    }

    public Set<AllergeneIngredient> getIngredients() {
        return allergeneIngredients;
    }

    public Allergene setIngredients(Set<AllergeneIngredient> ingredients) {
        this.allergeneIngredients = ingredients;
        return this;
    }
}
