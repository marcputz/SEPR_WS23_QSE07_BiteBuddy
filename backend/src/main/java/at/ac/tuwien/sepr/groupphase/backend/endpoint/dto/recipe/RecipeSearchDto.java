package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe;

public record RecipeSearchDto(
    String creator,
    String name,
    int page,
    int entriesPerPage
) {
}