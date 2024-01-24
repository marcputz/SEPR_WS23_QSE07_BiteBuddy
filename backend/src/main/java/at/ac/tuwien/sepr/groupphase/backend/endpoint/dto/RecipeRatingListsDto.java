package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record RecipeRatingListsDto(
    List<Long> likes,
    List<Long> dislikes
) {
}
