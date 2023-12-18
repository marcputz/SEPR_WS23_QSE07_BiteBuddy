package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * This class is used to map the {@link Ingredient} entity to the {@link IngredientDto} DTO and vice versa.
 */
@Mapper
public interface IngredientMapper {

    /**
     * Maps a {@link Ingredient} entity to a {@link IngredientDto} DTO.
     *
     * @param ingredient the ingredient to map
     * @return the mapped ingredientDto
     */
    @Named("ingredientDtoToIngredient")
    IngredientDto ingredientToIngredientDto(Ingredient ingredient);

    /**
     * Maps a {@link IngredientDto} DTO to a {@link Ingredient} entity.
     *
     * @param ingredientDto the ingredientDto to map
     * @return the mapped ingredient
     */
    @Named("ingredientToIngredientDto")
    Ingredient ingredientDtoToIngredient(IngredientDto ingredientDto);
}
