import {Address} from "../../../../customer/customer-common/api/datastructures/address.datastructure";

export class Party {
  public name: string;
  public uuid: string;
  public address: Address;
}

export class Flight {
  public flightNumber = "1234";

  public price = 2.20;
  public departureTime = "2014-12-03";
}
