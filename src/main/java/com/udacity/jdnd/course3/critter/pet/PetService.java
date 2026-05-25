package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.user.Customer;
import com.udacity.jdnd.course3.critter.user.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for Pet operations.
 *
 * Coordinates between PetRepository and CustomerRepository to handle
 * pet creation (which requires linking to an existing customer).
 *
 * Transaction boundary: All methods are @Transactional so that DB operations
 * are atomic. A failure mid-way (e.g., owner not found after saving pet)
 * will roll back the entire operation.
 *
 * This layer works exclusively with Pet/Customer entities — no DTOs.
 */
@Service
@Transactional
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Saves a new pet, linking it to the customer specified by ownerId.
     *
     * @param pet     the pet entity (populated from DTO in controller)
     * @param ownerId the ID of the customer who owns this pet
     * @return the persisted pet with generated ID
     * @throws IllegalArgumentException if no customer exists with the given ownerId
     */
    public Pet savePet(Pet pet, long ownerId) {
        Customer owner = customerRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No customer found with id: " + ownerId));
        pet.setOwner(owner);
        Pet savedPet = petRepository.save(pet);

        // Add the pet to the owner's pet list to keep the bidirectional
        // relationship in sync within the same transaction/session.
        owner.getPets().add(savedPet);
        customerRepository.save(owner);

        return savedPet;
    }

    /**
     * Retrieves a single pet by its ID.
     *
     * @param petId the ID of the pet
     * @return the found Pet entity
     * @throws IllegalArgumentException if no pet exists with the given ID
     */
    @Transactional(readOnly = true)
    public Pet getPetById(long petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No pet found with id: " + petId));
    }

    /**
     * Returns all pets stored in the system.
     */
    @Transactional(readOnly = true)
    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    /**
     * Returns all pets owned by the customer with the given ID.
     *
     * @param ownerId the customer ID
     * @return list of pets belonging to that customer
     */
    @Transactional(readOnly = true)
    public List<Pet> getPetsByOwner(long ownerId) {
        return petRepository.findByOwnerId(ownerId);
    }
}
