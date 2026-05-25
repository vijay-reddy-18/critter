package com.udacity.jdnd.course3.critter.pet;

import com.udacity.jdnd.course3.critter.user.Customer;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing a pet of any type.
 *
 * A pet belongs to exactly one Customer (owner). This is modeled with a
 * ManyToOne relationship where Pet holds the FK column (owner_id).
 *
 * PetType is stored as a STRING enum for clarity and resilience.
 *
 * birthDate uses LocalDate (not DateTime) because we only care about the
 * date, not the time of birth.
 */
@Entity
@Table(name = "pet")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The type of pet (CAT, DOG, LIZARD, etc.). Using Enum + STRING storage
     * so queries remain readable and addition of new pet types won't break
     * existing data via ordinal shifting.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PetType type;

    @Column(nullable = false, length = 500)
    private String name;

    /**
     * The customer who owns this pet.
     * EAGER fetch — owner id is always needed when converting Pet to PetDTO.
     * LAZY would cause LazyInitializationException when accessed in controller
     * layer after the service transaction has already closed.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private Customer owner;

    /**
     * Only the date matters for a pet's birthday — not time of day.
     */
    @Column
    private LocalDate birthDate;

    @Column(length = 2000)
    private String notes;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PetType getType() {
        return type;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Customer getOwner() {
        return owner;
    }

    public void setOwner(Customer owner) {
        this.owner = owner;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
