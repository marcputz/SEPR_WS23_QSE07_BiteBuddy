package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public record RecipeSearchDto(
    String creator,
    String name,
    int page,
    int entriesPerPage
) {
}