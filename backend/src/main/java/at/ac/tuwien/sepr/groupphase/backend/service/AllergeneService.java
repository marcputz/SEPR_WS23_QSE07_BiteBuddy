package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;

import java.util.List;

/**
 * This class is used to provide functionality for allergens.
 */
public interface AllergeneService {

    /**
    * Returns a list of all allergens in the database.
    *
    * @return all allergens
    */
    List<AllergeneDto> getAllAllergens();
}
