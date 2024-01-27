package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeListDto;

import java.util.List;

public record RecipeSearchResultDto(
    int page,
    int entriesPerPage,
    int numberOfPages,
    List<RecipeListDto> recipes
) {
}
