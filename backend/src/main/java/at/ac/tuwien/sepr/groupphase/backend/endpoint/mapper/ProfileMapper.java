package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

/**
 * This class is used to map the {@link Profile} entity to the {@link ProfileDto} DTO and vice versa.
 */
@Mapper
public interface ProfileMapper {

    /**
     * Maps a {@link Profile} entity to a {@link ProfileDto} DTO.
     * @param profile the profile to map
     * @return the mapped profileDto
     */
    @Named("profileToProfileDto")
    ProfileDto profileToProfileDto(Profile profile);

    /**
     * Maps a {@link ProfileDto} DTO to a {@link Profile} entity.
     * @param profileDto the profileDto to map
     * @return the mapped profile
     */
    @Named("profileDtoToProfile")
    Profile profileDtoToProfile(ProfileDto profileDto);
}
