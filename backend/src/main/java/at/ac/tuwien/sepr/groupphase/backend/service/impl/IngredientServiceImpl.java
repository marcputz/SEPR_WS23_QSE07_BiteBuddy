package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.IngredientDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.IngredientMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Ingredient;
import at.ac.tuwien.sepr.groupphase.backend.repository.IngredientRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.IngredientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IngredientServiceImpl implements IngredientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    public IngredientServiceImpl(IngredientRepository ingredientRepository, IngredientMapper ingredientMapper) {
        this.ingredientRepository = ingredientRepository;
        this.ingredientMapper = ingredientMapper;
    }

    @Override
    public List<IngredientDto> getAllIngredients() {
        List<Ingredient> allIngredients = ingredientRepository.findAll();

        // Filter out ingredients with "Filler" in the name
        List<Ingredient> filteredIngredients = allIngredients.stream()
            .filter(ingredient -> !ingredient.getName().contains("Filler"))
            .collect(Collectors.toList());

        // Map the filtered ingredients to DTOs
        return ingredientMapper.ingredientToListAllIngredientDtos(filteredIngredients);
    }

    @Override
    public Ingredient getById(long id) {
        Optional<Ingredient> i = this.ingredientRepository.findById(id);
        return i.orElse(null);
    }

    @Override
    public List<Ingredient> getByNameMatching(String name) {
        LOGGER.trace("getByNameMatching({})", name);
        return this.ingredientRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public List<String> getNamesMatching(String name) {
        List<Ingredient> matchingOnes = this.getByNameMatching(name);
        List<Ingredient> filteredIngredients = matchingOnes.stream()
            .filter(ingredient -> !ingredient.getName().contains("Filler"))
            .toList();
        List<String> result = new ArrayList<>();

        for (Ingredient ing : filteredIngredients) {
            result.add(ing.getName());

            if (result.size() >= 10) {
                return result;
            }
        }

        return result;
    }
}
