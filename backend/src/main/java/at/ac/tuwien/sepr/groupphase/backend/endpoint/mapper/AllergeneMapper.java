package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * This class is used to map the {@link Allergene} entity to the {@link AllergeneDto} DTO and vice versa.
 */
@Mapper
public interface AllergeneMapper {

    /**
     * Maps a {@link Allergene} entity to a {@link AllergeneDto} DTO.
     *
     * @param allergene the allergene to map
     * @return the mapped allergeneDto
     */
    @Named("allergeneToAllergeneDto")
    AllergeneDto allergeneToAllergeneDto(Allergene allergene);

    /**
     * Maps a {@link AllergeneDto} DTO to a {@link Allergene} entity.
     *
     * @param allergeneDto the allergeneDto to map
     * @return the mapped allergene
     */
    @Named("allergeneDtoToAllergene")
    Allergene allergeneDtoToAllergene(AllergeneDto allergeneDto);
}
