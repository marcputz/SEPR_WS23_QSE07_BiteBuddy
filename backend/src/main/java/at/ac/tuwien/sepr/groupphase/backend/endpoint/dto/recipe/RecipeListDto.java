package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe;

import java.util.List;

public record RecipeListDto(
    String creator,
    String name,
    Long id,
    long pictureId
) {
}
