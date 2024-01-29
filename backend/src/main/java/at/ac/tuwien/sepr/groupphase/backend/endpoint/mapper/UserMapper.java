package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserSettingsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * This class is used to map the {@link ApplicationUser} entity to the {@link UserSettingsDto} DTO.
 */
@Mapper
public interface UserMapper {

    /**
     * Maps a {@link ApplicationUser} entity to an {@link UserSettingsDto} DTO.
     *
     * @param user the ApplicationUser to map
     * @return the mapped UserSettingsDto
     */

    @Mapping(source = "activeProfile.id", target = "activeProfileId")
    UserSettingsDto toUserSettingsDto(ApplicationUser user);
}
