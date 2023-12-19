package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data object transfer for Profile entity to send information through the network and between layers.
 * To create java boilerplate code use lombok.
 */

public class ProfileDto {

    @JsonProperty("name")
    @NotNull(message = "Name must not be null")
    @Size(max = 255, min = 1)
    private String name;

    @JsonProperty("allergens")
    @NotNull(message = "Allergens must not be null")
    private List<AllergeneDto> allergens;

    @JsonProperty("ingredient")
    @NotNull(message = "Food preferences must not be null")
    private List<IngredientDto> ingredient;

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

    @Override
    public String toString() {
        return "ProfileDto{"
            + "name='" + name + '\''
            + ", allergens=" + allergens
            + ", ingredient=" + ingredient
            + "}";
    }

    public static final class ProfileDtoBuilder {
        private String name;
        private List<AllergeneDto> allergens;
        private List<IngredientDto> ingredient;

        private ProfileDtoBuilder() {}

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

        public ProfileDto build() {
            ProfileDto profileDto = new ProfileDto();
            profileDto.setName(name);
            profileDto.setAllergens(allergens);
            profileDto.setIngredient(ingredient);
            return profileDto;
        }
    }
}
