package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record ProfileListDto(
    Long id,
    String name,
    List<AllergeneDto> allergens,
    List<IngredientDto> ingredients,
    Long userId,
    String userName
) {
}