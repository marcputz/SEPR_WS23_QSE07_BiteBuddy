package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PictureDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.exception.AuthenticationException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.AuthenticationService;
import at.ac.tuwien.sepr.groupphase.backend.service.PictureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

/**
 * REST Endpoint for Pictures.
 *
 * @author Marc Putz
 */
@RestController
@RequestMapping(value = PictureEndpoint.BASE_PATH)
public class PictureEndpoint {

    public static final String BASE_PATH = "/api/v1/picture";
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final PictureService service;

    private final AuthenticationService authService;

    @Autowired
    public PictureEndpoint(PictureService service, AuthenticationService authService) {
        this.service = service;
        this.authService = authService;
    }

    /**
     * Retrieves a picture by its ID.
     *
     * @param id the picture ID to get.
     * @return the picture as its detail DTO.
     * @throws NotFoundException if no picture with given ID was found.
     * @author Marc Putz
     */
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public PictureDto getPicture(@RequestParam long id) throws NotFoundException {
        LOGGER.trace("getPicture({})", id);

        return this.service.getByIdAsDto(id);
    }

    /**
     * Creates a new picture.
     *
     * @param headers the http headers of the request.
     * @param pictureDto the data to process
     * @return the newly created picture as its detail DTO.
     * @throws AuthenticationException if the request isn't authenticated by a valid user.
     * @throws DataStoreException if the data store cannot handle the data.
     * @author Marc Putz
     */
    @PutMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public PictureDto createPicture(@RequestHeader HttpHeaders headers, @RequestBody PictureDto pictureDto) throws AuthenticationException, DataStoreException {
        LOGGER.trace("createPicture({})", pictureDto);

        this.authService.verifyAuthenticated(headers);

        Picture p = this.service.createPicture(pictureDto.getData(), pictureDto.getDescription());
        return this.service.getByIdAsDto(p.getId());
    }
}
