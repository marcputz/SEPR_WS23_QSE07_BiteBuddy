package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.recipe.RecipeGetByIdDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public record ProfileDetailDto(
    Long id,
    String name,
    ArrayList<AllergeneDto> allergens,
    ArrayList<IngredientDto> ingredients,
    ArrayList<RecipeGetByIdDto> liked,
    ArrayList<RecipeGetByIdDto> disliked,
    String user,
    Long userId,
    byte[] userPicture
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProfileDetailDto that = (ProfileDetailDto) o;
        return Objects.equals(id, that.id)
            && Objects.equals(name, that.name)
            && Objects.equals(allergens, that.allergens)
            && Objects.equals(ingredients, that.ingredients)
            && Objects.equals(liked, that.liked)
            && Objects.equals(disliked, that.disliked)
            && Objects.equals(user, that.user)
            && Objects.equals(userId, that.userId)
            && Arrays.equals(userPicture, that.userPicture);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, name, allergens, ingredients, liked, disliked, user, userId);
        result = 31 * result + Arrays.hashCode(userPicture);
        return result;
    }

    @Override
    public String toString() {
        String userPictureLength = (userPicture != null) ? String.valueOf(userPicture.length) : "null";
        return "ProfileDetailDto{"
            + "id=" + id
            + ", name='" + name + '\''
            + ", allergens=" + allergens
            + ", ingredients=" + ingredients
            + ", liked=" + liked
            + ", disliked=" + disliked
            + ", user='" + user + '\''
            + ", userId=" + userId
            + ", userPicture.length=" + userPictureLength
            + '}';
    }
}
