package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PictureDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;

/**
 * Service interface for managing Picture entities.
 *
 * @author Marc Putz
 */
public interface PictureService {

    /**
     * Gets a picture entity by its ID.
     *
     * @param id the picture ID.
     * @return the picture entity
     * @throws NotFoundException if the picture with given ID cannot be found in the data store.
     * @author Marc Putz
     */
    Picture getById(long id) throws NotFoundException;

    /**
     * Gets a picture dto by its ID.
     *
     * @param id the picture ID.
     * @return the picture dto, or NULL if no picture with given ID is found.
     * @throws NotFoundException if the picture with given ID cannot be found in the data store.
     * @author Marc Putz
     */
    PictureDto getByIdAsDto(long id) throws NotFoundException;

    /**
     * Creates a new image in the data store.
     *
     * @param imgData the picture data as a byte array.
     * @param description a description of the image.
     * @return the created picture.
     * @throws DataStoreException if the data store cannot process the new entity.
     * @author Marc Putz
     */
    Picture createPicture(byte[] imgData, String description) throws DataStoreException;

    /**
     * Creates a new image in the data store.
     *
     * @param imgData the picture data as a byte array.
     * @return the created picture.
     * @throws DataStoreException if the data store cannot process the new entity.
     * @author Marc Putz
     */
    Picture createPicture(byte[] imgData) throws DataStoreException;
}
