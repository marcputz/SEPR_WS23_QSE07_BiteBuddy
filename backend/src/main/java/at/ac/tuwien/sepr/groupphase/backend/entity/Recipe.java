package at.ac.tuwien.sepr.groupphase.backend.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
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
    private Long creatorId;

    @Lob
    @Column
    private String instructions;
    @Lob
    @Column
    private Long pictureId;

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

    public Long getPictureId() {
        return pictureId;
    }

    public void setPictureId(Long pictureId) {
        this.pictureId = pictureId;
    }

    public void setIngredients(Set<RecipeIngredient> ingredients) {
        this.recipeIngredients = ingredients;
    }

    public Set<RecipeIngredient> getIngredients() {
        return recipeIngredients;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public Recipe setCreatorId(Long userId) {
        this.creatorId = userId;
        return this;
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
            && Objects.equals(instructions, recipe.instructions)
            && Objects.equals(creatorId, recipe.creatorId);
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
            + "creator=" + creatorId
            + "instructions=" + instructions
            + '}';
    }

}