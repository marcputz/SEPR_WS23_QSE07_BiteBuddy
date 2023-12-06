package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;


import java.util.Objects;
import java.util.Set;

@Entity
public class Allergene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @OneToMany(mappedBy = "allergene")
    private Set<AllergeneIngredient> allergeneIngredients;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<AllergeneIngredient> getIngredients() {
        return allergeneIngredients;
    }

    public void setIngredients(Set<AllergeneIngredient> ingredients) {
        this.allergeneIngredients = ingredients;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Allergene allergene)) {
            return false;
        }
        return Objects.equals(id, allergene.id)
            && Objects.equals(name, allergene.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Allergene{"
            + "id=" + id
            + "name=" + name
            + '}';
    }
}
