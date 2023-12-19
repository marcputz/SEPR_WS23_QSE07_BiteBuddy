package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.ArrayList;

public record RecipeDetailsDto(
    Long id,
    String name,
    String description,
    ArrayList<String> ingredients,
    ArrayList<String> allergens,
    byte[] picture
){}
