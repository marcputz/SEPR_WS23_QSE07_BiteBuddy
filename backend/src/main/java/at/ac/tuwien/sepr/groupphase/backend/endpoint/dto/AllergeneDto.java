package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AllergeneDto {

    @JsonProperty("id")
    @NotNull(message = "Id must not be null")
    private Long id;

    @JsonProperty("name")
    @NotNull(message = "Name must not be null")
    private String name;

    @JsonProperty("allergeneIngredients")
    @NotNull(message = "AllergeneIngredients must not be null")
    private List<AllergeneIngredientDto> allergeneIngredients;

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

    public List<AllergeneIngredientDto> getAllergeneIngredients() {
        return allergeneIngredients;
    }

    public void setAllergeneIngredients(List<AllergeneIngredientDto> allergeneIngredients) {
        this.allergeneIngredients = allergeneIngredients;
    }

    @Override
    public String toString() {
        return "AllergeneDto{"
            + "id='" + id + '\''
            + "name='" + name + '\''
            + ", allergeneIngredients=" + allergeneIngredients
            + '}';
    }

    public static final class AllergeneDtoBuilder {
        private Long id;
        private String name;
        private List<AllergeneIngredientDto> allergeneIngredients;

        private AllergeneDtoBuilder() {
        }

        public static AllergeneDtoBuilder anAllergeneDto() {
            return new AllergeneDtoBuilder();
        }

        public AllergeneDtoBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public AllergeneDtoBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public AllergeneDtoBuilder withAllergeneIngredients(List<AllergeneIngredientDto> allergeneIngredients) {
            this.allergeneIngredients = allergeneIngredients;
            return this;
        }

        public AllergeneDto build() {
            AllergeneDto allergeneDto = new AllergeneDto();
            allergeneDto.setId(id);
            allergeneDto.setName(name);
            allergeneDto.setAllergeneIngredients(allergeneIngredients);
            return allergeneDto;
        }
    }
}
