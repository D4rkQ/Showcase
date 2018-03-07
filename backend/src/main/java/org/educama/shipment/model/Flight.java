package org.educama.shipment.model;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * This represents the flight entity used for database persistence.
 */

// @Table(uniqueConstraints = { @UniqueConstraint(columnNames = "flightNumber") })
@SuppressWarnings("serial")
@Entity
public class Flight extends AbstractPersistable<Long> {

    // @Column(unique = true)
    @NotNull
    public String flightNumber;

    @NotNull
    public String airline;

    @NotNull
    public String departureAirport;

    @NotNull
    public String destinationAirport;

    @NotNull
    public String departureTime;

    @NotNull
    public String destinationTime;

    @NotNull
    public double price;


    /**
     * Constructor for JPA.
     */
    protected Flight() {
        //empty
    }

    public Flight(String flightNumber, String airline, String departureAirport, String destinationAirport,
                  String departureTime, String destinationTime, double price) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.departureAirport = departureAirport;
        this.destinationAirport = destinationAirport;
        this.departureTime = departureTime;
        this.destinationTime = destinationTime;
        this.price = price;
    }
}
