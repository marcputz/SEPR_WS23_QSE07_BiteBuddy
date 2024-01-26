package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToMany
    private Set<Allergene> allergens = new HashSet<>();

    @ManyToMany
    private Set<Ingredient> ingredient = new HashSet<>();

    @ManyToMany
    private Set<Recipe> liked = new HashSet<>();

    @ManyToMany
    private Set<Recipe> disliked = new HashSet<>();

    @ManyToOne
    private ApplicationUser user;

    public Profile() {}

    public Profile(Long id, String name, Set<Allergene> allergens, Set<Ingredient> ingredient, ApplicationUser user) {
        this.id = id;
        this.name = name;
        this.allergens = allergens;
        this.ingredient = ingredient;
        this.user = user;
    }

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

    public Set<Allergene> getAllergens() {
        return allergens;
    }

    public void setAllergens(Set<Allergene> allergens) {
        this.allergens = allergens;
    }

    public Set<Ingredient> getIngredient() {
        return ingredient;
    }

    public void setIngredient(Set<Ingredient> ingredient) {
        this.ingredient = ingredient;
    }

    public Set<Recipe> getLiked() {
        return liked;
    }

    public void setLiked(Set<Recipe> liked) {
        this.liked = liked;
    }

    public Set<Recipe> getDisliked() {
        return disliked;
    }

    public void setDisliked(Set<Recipe> disliked) {
        this.disliked = disliked;
    }

    public void setUser(ApplicationUser user) {
        this.user = user;
    }

    public ApplicationUser getUser() {
        return user;
    }

}
