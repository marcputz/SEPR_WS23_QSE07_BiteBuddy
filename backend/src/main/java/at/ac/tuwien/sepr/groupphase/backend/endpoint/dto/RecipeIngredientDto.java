package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.entity.FoodUnit;

public record RecipeIngredientDto(
    String name,
    Float amount,
    FoodUnit unit
) {
}
