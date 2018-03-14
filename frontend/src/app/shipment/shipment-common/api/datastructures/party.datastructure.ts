import {Address} from "../../../../customer/customer-common/api/datastructures/address.datastructure";

export class Party {
  public name: string;
  public uuid: string;
  public address: Address;
}

export class Flight {
  public flightNumber = "10243";
  public airline = "LH";
  public departureAirport = "FRA";
  public destinationAirport = "STR";
  public departureTime = "10;45";
  public destinationTime = "12:45";
  public price = 100.12;
}
