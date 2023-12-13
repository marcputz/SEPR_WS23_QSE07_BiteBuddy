package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.type.AllergensEU;
import at.ac.tuwien.sepr.groupphase.backend.type.FoodPreference;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Data object transfer for Profile entity to send information through the network and between layers.
 * To create java boilerplate code use lombok.
 */
@RequiredArgsConstructor
@Getter
@ToString
public class ProfileDto {

    @NotNull(message = "Username must not be null")
    @Size(max = 255, min = 1)
    private String userName;

    @NotNull(message = "Allergens must not be null")
    private List<AllergensEU> allergens;

    @NotNull(message = "Food preferences must not be null")
    private List<FoodPreference> foodPreference;
}
