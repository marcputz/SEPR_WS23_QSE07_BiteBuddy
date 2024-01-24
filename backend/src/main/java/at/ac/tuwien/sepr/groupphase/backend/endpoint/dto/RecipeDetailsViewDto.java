package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.ArrayList;

public record RecipeDetailsViewDto(
    Long id,
    String name,
    String description,
    ArrayList<RecipeIngredientDto> ingredients,
    ArrayList<String> allergens,
    byte[] picture,
    int rating
){}
