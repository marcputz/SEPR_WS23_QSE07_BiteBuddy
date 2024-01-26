package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe;

import java.util.ArrayList;

public record RecipeDetailsDto(
    Long id,
    String name,
    String description,
    ArrayList<RecipeIngredientDto> ingredients,
    ArrayList<String> allergens,
    Long pictureId
){}
