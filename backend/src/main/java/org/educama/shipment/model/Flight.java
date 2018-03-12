package org.educama.shipment.model;

import javax.persistence.Embeddable;

/**
 * This represents the address entity used for database persistence.
 */
@Embeddable
public class Flight {

    public String flightNumber;


    /**
     * Constructor for JPA.
     */
    public Flight() {
        //empty
    }

    public Flight(String flightNumber) {
        this.flightNumber = flightNumber;
    }
}

