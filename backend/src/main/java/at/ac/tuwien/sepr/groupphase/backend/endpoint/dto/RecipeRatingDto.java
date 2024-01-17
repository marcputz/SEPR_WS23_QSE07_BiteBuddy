package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record RecipeRatingDto(
    Long recipeId,
    Long userId,
    int rating
) {}