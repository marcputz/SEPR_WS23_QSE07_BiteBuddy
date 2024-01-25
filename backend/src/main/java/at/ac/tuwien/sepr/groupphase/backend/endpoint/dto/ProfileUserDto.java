package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Data object transfer for Profile entity to send information through the network and between layers.
 * To create java boilerplate code use lombok.
 */

public class ProfileUserDto {

    @JsonProperty("id")
    @NotNull(message = "Id must not be null")
    private Long id;

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

    @JsonProperty("user")
    @NotNull(message = "User id must not be null")
    private ApplicationUser user;

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

    public ApplicationUser getUser() {
        return user;
    }

    public void setUser(ApplicationUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ProfileDto{"
            + "name='" + name + '\''
            + ", allergens=" + allergens
            + ", ingredient=" + ingredient
            + ", user=" + user
            + "}";
    }

    public static final class ProfileDtoBuilder {
        private String name;
        private List<AllergeneDto> allergens;
        private List<IngredientDto> ingredient;
        private ApplicationUser user;

        private ProfileDtoBuilder() {
        }

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

        public ProfileDtoBuilder withUser(ApplicationUser user) {
            this.user = user;
            return this;
        }

        public ProfileUserDto build() {
            ProfileUserDto profileDto = new ProfileUserDto();
            profileDto.setName(name);
            profileDto.setAllergens(allergens);
            profileDto.setIngredient(ingredient);
            profileDto.setUser(user);
            return profileDto;
        }
    }
}
