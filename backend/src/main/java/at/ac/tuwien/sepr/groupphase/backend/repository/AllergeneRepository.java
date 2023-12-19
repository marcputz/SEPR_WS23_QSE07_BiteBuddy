package at.ac.tuwien.sepr.groupphase.backend.repository;

import at.ac.tuwien.sepr.groupphase.backend.entity.Allergene;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface AllergeneRepository extends JpaRepository<Allergene, Long> {
    Set<Allergene> findAllByAllergeneIngredientsIngredientId(long l);

    List<Allergene> findAll();
}
