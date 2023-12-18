package at.ac.tuwien.sepr.groupphase.backend.entity;


import at.ac.tuwien.sepr.groupphase.backend.type.AllergensEU;
import at.ac.tuwien.sepr.groupphase.backend.type.FoodPreference;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "profile")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String userName;

    /*
    @ElementCollection
    @CollectionTable(name = "allergene", joinColumns = @JoinColumn(name = "id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "allergene", nullable = false)
    private Set<AllergensEU> allergens;

    @ElementCollection
    @CollectionTable(name = "food_preference", joinColumns = @JoinColumn(name = "id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "food_preference", nullable = false)
    private Set<FoodPreference> foodPreferences;
     */
}
