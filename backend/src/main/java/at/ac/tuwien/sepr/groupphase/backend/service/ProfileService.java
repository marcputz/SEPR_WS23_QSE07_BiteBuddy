package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ProfileDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

public interface ProfileService {
    /**
     * Save a single profile entry.
     *
     * @param profileDto to save
     * @return saved profile entry
     * @throws ValidationException if the profileDto is not valid
     */
    ProfileDto saveProfile(ProfileDto profileDto) throws ValidationException;
}
