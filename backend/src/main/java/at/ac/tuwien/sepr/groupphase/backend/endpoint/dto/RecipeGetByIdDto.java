package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.ArrayList;

public record RecipeGetByIdDto(
    Long id,
    Long userId
) {
}
