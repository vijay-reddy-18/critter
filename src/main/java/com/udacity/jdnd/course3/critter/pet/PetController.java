package com.udacity.jdnd.course3.critter.pet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles web requests related to Pets.
 *
 * DTO ↔ Entity mapping occurs here in the controller layer.
 * PetService is called with entity objects only (no DTOs passed to service).
 */
@RestController
@RequestMapping("/pet")
public class PetController {

    @Autowired
    private PetService petService;

    /**
     * Creates and saves a new pet for the owner specified in the DTO.
     *
     * @param petDTO the incoming pet data
     * @return the saved pet as a DTO with its generated ID
     */
    @PostMapping
    public PetDTO savePet(@RequestBody PetDTO petDTO) {
        Pet pet = convertDTOtoPet(petDTO);
        Pet savedPet = petService.savePet(pet, petDTO.getOwnerId());
        return convertPetToDTO(savedPet);
    }

    /**
     * Retrieves a single pet by its ID.
     *
     * @param petId the pet's ID
     * @return the pet as a DTO
     */
    @GetMapping("/{petId}")
    public PetDTO getPet(@PathVariable long petId) {
        return convertPetToDTO(petService.getPetById(petId));
    }

    /**
     * Returns all pets in the system.
     *
     * @return list of all pets as DTOs
     */
    @GetMapping
    public List<PetDTO> getPets(){
        return petService.getAllPets().stream()
                .map(this::convertPetToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns all pets owned by a specific customer.
     *
     * @param ownerId the customer's ID
     * @return list of that customer's pets as DTOs
     */
    @GetMapping("/owner/{ownerId}")
    public List<PetDTO> getPetsByOwner(@PathVariable long ownerId) {
        return petService.getPetsByOwner(ownerId).stream()
                .map(this::convertPetToDTO)
                .collect(Collectors.toList());
    }

    // ==================== DTO Mapping Helpers ====================

    /**
     * Converts a PetDTO (incoming request) into a Pet entity.
     * The ownerId is handled separately by the service layer.
     */
    private Pet convertDTOtoPet(PetDTO petDTO) {
        Pet pet = new Pet();
        pet.setType(petDTO.getType());
        pet.setName(petDTO.getName());
        pet.setBirthDate(petDTO.getBirthDate());
        pet.setNotes(petDTO.getNotes());
        return pet;
    }

    /**
     * Converts a Pet entity into a PetDTO (outgoing response).
     * Extracts the ownerId from the owner relationship.
     */
    private PetDTO convertPetToDTO(Pet pet) {
        PetDTO petDTO = new PetDTO();
        petDTO.setId(pet.getId());
        petDTO.setType(pet.getType());
        petDTO.setName(pet.getName());
        petDTO.setBirthDate(pet.getBirthDate());
        petDTO.setNotes(pet.getNotes());
        if (pet.getOwner() != null) {
            petDTO.setOwnerId(pet.getOwner().getId());
        }
        return petDTO;
    }
}
