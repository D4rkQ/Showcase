package org.educama.shipment.api.datastructure;

import org.educama.shipment.model.Flight;

/**
 * Re-usable data structure used by resources.
 */
public class FlightDS {

    public String flightNumber;
    public double price;
    public String departureTime;

    private FlightDS() {
        // empty
    }

    public FlightDS(Flight flight) {
        this.flightNumber = flight.flightNumber;
        this.price = flight.price;
        this.departureTime = flight.departureTime;
    }

    public Flight toFlight() {
                return new Flight(this.flightNumber, this.price, this.departureTime);
    }

}
