package org.educama.shipment.api.datastructure;


import org.educama.shipment.model.Flight;


/**
 * Re-usable data structure used by resources.
 */
public class FlightDS {

   public String flightNumber;
   public String airline;
   public String departureAirport;
   public String destinationAirport;
   public double price;
   public String departureTime;
   public String destinationTime;

    /*
  * For Jackson Parser
  */
    private FlightDS() {
        // empty
    }

   public FlightDS(Flight flight) {
       this.flightNumber = flight.flightNumber;
       this.airline = flight.airline;
       this.departureAirport = flight.departureAirport;
       this.destinationAirport = flight.destinationAirport;
       this.departureTime = flight.departureTime;
       this.destinationTime = flight.destinationTime;
       this.price = flight.price;
   }

    public FlightDS(String flightNumber, String airline, String departureAirport, String destinationAirport,
                    String departureTime, String destinationTime, double price) {
        super();
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.departureAirport = departureAirport;
        this.destinationAirport = destinationAirport;
        this.departureTime = departureTime;
        this.destinationTime = destinationTime;
        this.price = price;
    }

    public Flight toFlight() {
        return new Flight(this.flightNumber, this.airline, this.departureAirport, this.destinationAirport,
                this.departureTime, this.destinationTime, this.price);
    }
}
