package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeGetByIdDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * This class is used to map the {@link Recipe} entity to the {@link RecipeDto} DTO and vice versa.
 */
@Mapper
public interface RecipeMapper {

    /**
     * Maps a {@link Recipe} entity to an {@link RecipeDto} DTO.
     *
     * @param recipe the Recipe to map
     * @return the mapped RecipeDto
     */
    @Named("allergeneToAllergeneDto")
    RecipeDto recipeToRecipeDto(Recipe recipe);

    /**
     * Maps a {@link Recipe} entity to an {@link RecipeGetByIdDto} DTO.
     *
     * @param recipe the Recipe to map
     * @return the mapped RecipeGetByIdDto
     */

    @Named("recipeToRecipeGetByIdDto")
    RecipeGetByIdDto recipeToRecipeGetByIdDto(Recipe recipe);
}
