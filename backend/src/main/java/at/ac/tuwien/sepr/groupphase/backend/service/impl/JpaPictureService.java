package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PictureDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.repository.PictureRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PictureService;
import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

/**
 * JPA implementation of Picture Service.
 *
 * @author Marc Putz
 */
@Service
public class JpaPictureService implements PictureService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PictureRepository repo;

    @Autowired
    public JpaPictureService(PictureRepository repo) {
        this.repo = repo;
    }

    @Override
    public Picture getById(long id) {
        LOGGER.trace("getById({})", id);
        return repo.findById(id).orElse(null);
    }

    @Override
    public PictureDto getByIdAsDto(long id) {
        LOGGER.trace("getByIdAsDto({})", id);
        return entityToDto(this.getById(id));
    }

    @Override
    public Picture createPicture(byte[] imgData, String description) throws DataStoreException {
        LOGGER.trace("createPicture()"); // do NOT show img data in logger!

        Picture p = new Picture()
            .setData(imgData)
            .setDescription(description);

        try {
            return this.repo.save(p);
        } catch (JDBCException | DataIntegrityViolationException e) {
            throw new DataStoreException("Could not save picture to the data store.", e);
        }
    }

    @Override
    public Picture createPicture(byte[] imgData) throws DataStoreException {
        LOGGER.trace("createPicture()"); // do NOT show img data in logger

        return this.createPicture(imgData, null);
    }

    /**
     * Helper function to convert picture entities to their respective DTOs.
     *
     * @param pic the picture entity to convert.
     * @return a DTO of the picture.
     * @author Marc Putz
     */
    private PictureDto entityToDto(Picture pic) {
        LOGGER.trace("entityToDto({})", pic);

        if (pic == null) {
            return null;
        }

        return new PictureDto()
            .setId(pic.getId())
            .setData(pic.getData())
            .setDescription(pic.getDescription());
    }
}
