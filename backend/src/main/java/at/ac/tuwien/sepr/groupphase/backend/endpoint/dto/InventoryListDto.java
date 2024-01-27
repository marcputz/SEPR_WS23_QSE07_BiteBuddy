package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.List;

public record InventoryListDto(
    List<InventoryIngredientDto> missing,
    List<InventoryIngredientDto> available
) {
}
