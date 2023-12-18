package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class RecipeRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    @Column
    private int rating;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setApplicationUser(ApplicationUser user) {
        this.user = user;
    }

    public ApplicationUser getApplicationUser() {
        return user;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRaiting(int raiting) {
        this.rating = raiting;
    }

    public int getRaiting() {
        return rating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecipeRating recipeRating)) {
            return false;
        }
        return Objects.equals(id, recipeRating.id)
            && Objects.equals(recipe, recipeRating.recipe)
            && Objects.equals(user, recipeRating.user)
            && Objects.equals(rating, recipeRating.rating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recipe, user);
    }

    @Override
    public String toString() {
        if(rating == 0){
            return "RecipeRaiting{"
                + "id=" + id
                + "recipe=" + recipe
                + "user=" + user
                + "rating=" + "disliked"
                + '}';
        }
        else if(rating == 1){
            return "RecipeRaiting{"
                + "id=" + id
                + "recipe=" + recipe
                + "user=" + user
                + "rating=" + "liked"
                + '}';
        }
        return "RecipeRaiting{"
            + "id=" + id
            + "recipe=" + recipe
            + "user=" + user
            + '}';

    }
}
