package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneIngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.AllergeneIngredient;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * This class is used to map the {@link AllergeneIngredient} entity to the {@link AllergeneIngredientDto} DTO and vice versa.
 */
@Mapper
public interface AllergeneIngredientMapper {
    /**
     * Maps a {@link AllergeneIngredient} entity to a {@link AllergeneIngredientDto} DTO.
     *
     * @param allergeneIngredientDto the allergeneIngredientDto to map
     * @return the mapped allergeneIngredient
     */
    @Named("allergeneIngredientToAllergeneIngredientDto")
    AllergeneIngredientDto allergeneIngredientToAllergeneIngredientDto(AllergeneIngredient allergeneIngredientDto);

    @Named("allergeneIngredientDtoToAllergeneIngredients")
    AllergeneIngredient allergeneIngredientDtoToAllergeneIngredient(AllergeneIngredientDto allergeneIngredientDto);
}
