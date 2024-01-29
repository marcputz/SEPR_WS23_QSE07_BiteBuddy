package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe;

public record RecipeListDto(
    String creator,
    String name,
    Long id,
    Long pictureId
) {
}
