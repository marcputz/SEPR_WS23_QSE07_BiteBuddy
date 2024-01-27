package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record ProfileEditDto(
    Long id,
    String name,
    List<AllergeneDto> allergens,
    List<IngredientDto> ingredient,
    Long userId


) {
}
