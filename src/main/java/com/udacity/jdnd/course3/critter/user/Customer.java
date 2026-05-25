package com.udacity.jdnd.course3.critter.user;

import com.udacity.jdnd.course3.critter.pet.Pet;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a customer (pet owner).
 *
 * Uses composition rather than inheritance — Customers and Employees are
 * treated as distinct entity types with their own tables. This avoids the
 * complexity of joined-table or single-table inheritance strategies and
 * keeps the schema simple and queryable.
 *
 * A Customer owns zero or more Pets. The relationship is bidirectional:
 * Pet holds the FK (owner_id), and Customer maps it with mappedBy="owner".
 */
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 500)
    private String name;

    /**
     * Phone number stored as a String (not int/long) to handle formats like
     * "123-456-789", "+1 (555) 000-0000", or leading zeros.
     */
    @Column(length = 50)
    private String phoneNumber;

    @Column(length = 2000)
    private String notes;

    /**
     * One customer may own many pets.
     * CascadeType.ALL ensures pets are persisted/removed with the customer.
     * FetchType.LAZY avoids loading all pets unnecessarily.
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Pet> pets = new ArrayList<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }
}
