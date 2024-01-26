package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.PictureDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Picture;
import at.ac.tuwien.sepr.groupphase.backend.service.PictureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired
    public PictureEndpoint(PictureService service) {
        this.service = service;
    }

    @GetMapping()
    public PictureDto getPicture(@RequestParam long id) {
        LOGGER.trace("getPicture({})", id);

        return this.service.getByIdAsDto(id);
    }

    @PutMapping()
    public PictureDto setPicture(@RequestBody PictureDto pictureDto) {
        LOGGER.trace("setPicture({})", pictureDto);

        Picture p = this.service.createPicture(pictureDto.getData(), pictureDto.getDescription());
        return this.service.getByIdAsDto(p.getId());
    }
}
