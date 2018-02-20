package org.educama.shipment.model;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.persistence.UniqueConstraint;
import javax.persistence.Table;
import java.time.Instant;

/**
 * This represents the flight entity used for database persistence.
 */
@SuppressWarnings("serial")
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "flightNumber") })
public class Flight extends AbstractPersistable<Long> {

    @NotNull
    @Column(unique = true)
    public String flightNumber;

    @NotNull
    public String airline;

    @NotNull
    public String departureAirport;

    @NotNull
    public String destinationAirport;

    @NotNull
    public Instant departureTime;

    @NotNull
    public Instant destinationTime;

    @NotNull
    public double price;


    /**
     * Constructor for JPA.
     */
    protected Flight() {
        //empty
    }

    public Flight(String flightNumber, String airline, String departureAirport, String destinationAirport,
                  Instant departureTime, Instant destinationTime, double price) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.departureAirport = departureAirport;
        this.destinationAirport = destinationAirport;
        this.departureTime = departureTime;
        this.destinationTime = destinationTime;
        this.price = price;
    }
}
