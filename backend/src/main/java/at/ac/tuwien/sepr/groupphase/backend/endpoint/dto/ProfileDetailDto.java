package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.util.ArrayList;

public record ProfileDetailDto(
    Long id,
    String name,
    ArrayList<String> allergens,
    ArrayList<String> ingredients,
    ArrayList<RecipeProfileViewDto> liked,
    ArrayList<RecipeProfileViewDto> disliked,
    String user,
    Long userId

) {
}
