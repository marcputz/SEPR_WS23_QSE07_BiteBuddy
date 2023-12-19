package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public class IngredientDto {

    @JsonProperty("id")
    @NotNull(message = "Id must not be null")
    private Long id;

    @JsonProperty("name")
    @NotNull(message = "Name must not be null")
    private String name;

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

    @Override
    public String toString() {
        return "IngredientDto{"
            + "id='" + id + '\''
            + "name='" + name + '\''
            + '}';
    }

    public static final class IngredientDtoBuilder {
        private Long id;
        private String name;

        private IngredientDtoBuilder() {
        }

        public static IngredientDtoBuilder anIngredientDto() {
            return new IngredientDtoBuilder();
        }

        public IngredientDtoBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public IngredientDtoBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public IngredientDto build() {
            IngredientDto ingredientDto = new IngredientDto();
            ingredientDto.setId(id);
            ingredientDto.setName(name);
            return ingredientDto;
        }
    }
}
