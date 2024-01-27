package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe;

import at.ac.tuwien.sepr.groupphase.backend.entity.FoodUnit;

public record RecipeIngredientDto(
    // WARNING: this name can match Ingredient or RecipeIngredientDetails.ingredient
    String name,
    Float amount,
    FoodUnit unit
) {
}
