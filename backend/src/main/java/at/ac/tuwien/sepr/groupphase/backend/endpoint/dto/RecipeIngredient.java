package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record RecipeIngredient(
    Long id,
    String name,
    String amount
) {
}
