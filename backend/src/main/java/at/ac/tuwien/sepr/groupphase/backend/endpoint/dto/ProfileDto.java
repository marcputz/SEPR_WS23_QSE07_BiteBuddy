package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data object transfer for Profile entity to send information through the network and between layers.
 */

public class ProfileDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    @NotNull(message = "Name must not be null")
    @Size(max = 255, min = 2)
    private String name;

    @JsonProperty("allergens")
    @NotNull(message = "Allergens must not be null")
    private List<AllergeneDto> allergens;

    @JsonProperty("ingredient")
    @NotNull(message = "Food preferences must not be null")
    private List<IngredientDto> ingredient;

    @JsonProperty("userId")
    @NotNull(message = "User id must not be null")
    private Long userId;

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

    public List<AllergeneDto> getAllergens() {
        return allergens;
    }

    public void setAllergens(List<AllergeneDto> allergens) {
        this.allergens = allergens;
    }

    public List<IngredientDto> getIngredient() {
        return ingredient;
    }

    public void setIngredient(List<IngredientDto> ingredient) {
        this.ingredient = ingredient;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "ProfileDto{"
            + "name='" + name + '\''
            + ", allergens=" + allergens
            + ", ingredient=" + ingredient
            + ", userId=" + userId
            + "}";
    }

    public static final class ProfileDtoBuilder {
        private String name;
        private List<AllergeneDto> allergens;
        private List<IngredientDto> ingredient;
        private Long userId;

        public static ProfileDtoBuilder aProfileDto() {
            return new ProfileDtoBuilder();
        }

        public ProfileDtoBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ProfileDtoBuilder withAllergens(List<AllergeneDto> allergens) {
            this.allergens = allergens;
            return this;
        }

        public ProfileDtoBuilder withIngredient(List<IngredientDto> ingredient) {
            this.ingredient = ingredient;
            return this;
        }

        public ProfileDtoBuilder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public ProfileDto build() {
            ProfileDto profileDto = new ProfileDto();
            profileDto.setName(name);
            profileDto.setAllergens(allergens);
            profileDto.setIngredient(ingredient);
            profileDto.setUserId(userId);
            return profileDto;
        }
    }
}
