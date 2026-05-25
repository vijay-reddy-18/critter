package com.udacity.jdnd.course3.critter.pet;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Pet entity persistence operations.
 *
 * Spring Data JPA automatically generates implementations for these
 * method signatures based on naming conventions (derived queries).
 *
 * All pet-related queries are centralized here — methods that return
 * Pets belong in the Pet repository, not in CustomerRepository.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    /**
     * Finds all pets belonging to a specific owner.
     * Derived from: Pet has an 'owner' field; Customer has an 'id' field.
     */
    List<Pet> findByOwnerId(long ownerId);
}
