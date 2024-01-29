package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileUserDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.Set;

/**
 * This class is used to map the {@link Profile} entity to the {@link ProfileDto} DTO and vice versa.
 */
@Mapper
public interface ProfileMapper {

    /**
     * Maps a {@link Profile} entity to a {@link ProfileDto} DTO.
     *
     * @param profile the profile to map
     * @return the mapped profileDto
     */
    @Named("profileToProfileDto")
    @Mapping(source = "user.id", target = "userId")
    ProfileDto profileToProfileDto(Profile profile);

    /**
     * Maps a {@link ProfileDto} DTO to a {@link Profile} entity.
     *
     * @param profileDto the profileDto to map
     * @return the mapped profile
     */
    @Named("profileDtoToProfile")
    Profile profileDtoToProfile(ProfileUserDto profileDto);

    @Mapping(target = "allergens", source = "allergens", qualifiedByName = "allergeneSetToAllergeneDtoList")
    @Mapping(target = "ingredients", source = "ingredient", qualifiedByName = "ingredientSetToIngredientDtoList")
    @Mapping(source = "user.nickname", target = "user")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.userPicture", target = "userPicture")
    ProfileDetailDto profileToProfileDetailDto(Profile profile);

    /**
     * Maps a Set of {@link Allergene} to a List of {@link AllergeneDto}.
     *
     * @param allergens the Set of Allergene to map
     * @return the mapped Arraylist of AllergeneDto
     */
    @Named("allergeneSetToAllergeneDtoList")
    ArrayList<AllergeneDto> allergeneSetToAllergeneDtoList(Set<Allergene> allergens);

    /**
     * Maps a set of {@link Ingredient} to a list of {@link IngredientDto}.
     *
     * @param ingredients the Set of Ingredient to map
     * @return the mapped Arraylist of IngredientDto
     */
    @Named("ingredientSetToIngredientDtoList")
    ArrayList<IngredientDto> ingredientSetToIngredientDtoList(Set<Ingredient> ingredients);
}
