package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe;

import java.time.LocalDateTime;

public record RecipeDto(
    Long id,
    LocalDateTime creationDate,
    String creator,
    String name,
    String instructions,
    Long pictureId
) {
}
