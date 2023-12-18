package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class AllergeneIngredientDto {

    @JsonProperty("allergene")
    @NotNull(message = "Allergene must not be null")
    private AllergeneDto allergene;

    @JsonProperty("ingredient")
    @NotNull(message = "Ingredient must not be null")
    private IngredientDto ingredient;

    public AllergeneDto getAllergene() {
        return allergene;
    }

    public void setAllergene(AllergeneDto allergene) {
        this.allergene = allergene;
    }

    public IngredientDto getIngredient() {
        return ingredient;
    }

    public void setIngredient(IngredientDto ingredient) {
        this.ingredient = ingredient;
    }

    @Override
    public String toString() {
        return "AllergeneIngredientDto{" +
            "allergene=" + allergene +
            ", ingredient=" + ingredient +
            '}';
    }

    public static final class AllergeneIngredientDtoBuilder {
        private AllergeneDto allergene;
        private IngredientDto ingredient;

        private AllergeneIngredientDtoBuilder() {
        }

        public static AllergeneIngredientDtoBuilder anAllergeneIngredientDto() {
            return new AllergeneIngredientDtoBuilder();
        }

        public AllergeneIngredientDtoBuilder withAllergene(AllergeneDto allergene) {
            this.allergene = allergene;
            return this;
        }

        public AllergeneIngredientDtoBuilder withIngredient(IngredientDto ingredient) {
            this.ingredient = ingredient;
            return this;
        }

        public AllergeneIngredientDto build() {
            AllergeneIngredientDto allergeneIngredientDto = new AllergeneIngredientDto();
            allergeneIngredientDto.setAllergene(allergene);
            allergeneIngredientDto.setIngredient(ingredient);
            return allergeneIngredientDto;
        }
    }
}
