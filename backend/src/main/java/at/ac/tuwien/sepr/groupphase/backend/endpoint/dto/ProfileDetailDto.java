package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeGetByIdDto;

import java.util.ArrayList;

public record ProfileDetailDto(
    Long id,
    String name,
    ArrayList<AllergeneDto> allergens,
    ArrayList<IngredientDto> ingredients,
    ArrayList<RecipeGetByIdDto> liked,
    ArrayList<RecipeGetByIdDto> disliked,
    String user,
    Long userId

) {
}
