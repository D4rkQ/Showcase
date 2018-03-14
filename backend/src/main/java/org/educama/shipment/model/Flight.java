package org.educama.shipment.model;

import javax.persistence.Embeddable;

/**
 * This represents the address entity used for database persistence.
 */
@Embeddable
public class Flight {

    public String flightNumber;
    public double price;
    public String departureTime;

    /**
     * Constructor for JPA.
     */
    public Flight() {
        //empty
    }

    public Flight(String flightNumber, double price, String departureTime) {
        this.flightNumber = flightNumber;
        this.price = price;
        this.departureTime = departureTime;

    }
}

