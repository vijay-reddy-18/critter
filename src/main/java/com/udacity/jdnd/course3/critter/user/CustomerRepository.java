package com.udacity.jdnd.course3.critter.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Customer entity persistence operations.
 *
 * Provides standard CRUD via JpaRepository plus a derived query to find
 * a customer by one of their pet IDs. The derived query navigates:
 *   Customer → pets (List<Pet>) → Pet.id
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Finds the customer who owns the pet with the given ID.
     * Derived query path: Customer.pets -> Pet.id
     */
    Customer findByPetsId(long petId);
}
