package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;

import java.util.List;

/**
 * This class is used to provide functionality for ingredients.
 */
public interface IngredientService {

  /**
   * Returns a list of all ingredients in the database.
   *
   * @return all ingredients
   */
  List<IngredientDto> getAllIngredients();
}
