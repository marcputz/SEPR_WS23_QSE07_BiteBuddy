package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.ArrayList;

public record RecipeDetailsDto(
    Long id,
    String name,
    String creatorName,
    String description,
    ArrayList<RecipeIngredientDto> ingredients,
    ArrayList<String> allergens,
    byte[] picture
){}
