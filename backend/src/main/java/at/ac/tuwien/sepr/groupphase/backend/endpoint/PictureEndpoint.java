package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PictureDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.PictureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    public PictureEndpoint(PictureService service) {
        this.service = service;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping()
    public PictureDto getPicture(@RequestParam long id) throws NotFoundException {
        LOGGER.trace("getPicture({})", id);

        return this.service.getByIdAsDto(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping()
    public PictureDto createPicture(@RequestBody PictureDto pictureDto) throws DataStoreException {
        LOGGER.trace("createPicture({})", pictureDto);

        Picture p = this.service.createPicture(pictureDto.getData(), pictureDto.getDescription());
        return this.service.getByIdAsDto(p.getId());
    }
}
