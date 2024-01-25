package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RecipeGetByIdDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Recipe;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper
public interface RecipeMapper {
    @Named("allergeneToAllergeneDto")
    RecipeDto recipeToRecipeDto(Recipe recipe);

    @Named("recipeToRecipeGetByIdDto")
    RecipeGetByIdDto recipeToRecipeGetByIdDto(Recipe recipe);
}
