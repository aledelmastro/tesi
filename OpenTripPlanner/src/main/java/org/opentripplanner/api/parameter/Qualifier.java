package org.opentripplanner.api.parameter;

public enum Qualifier {
  /** The vehicle has to be rented */
  RENT,
  /** The vehicle has to be parked before proceeding */
  PARK,
  /**
   * The user is picked up by the vehicle.
   */
  PICKUP,
  /**
   * The user is dropped off from the vehicle.
   */
  DROPOFF,
  /**
   * The mode is used for the access part of the search.
   */
  ACCESS,
  /**
   * The mode is used for the egress part of the search.
   */
  EGRESS,
  /**
   * The mode is used for the direct street search.
   */
  DIRECT,

  /**
   * Indica che il mezzo può essere utilizzato fino
   * a una fermata/stazione di un mezzo pubblico.
   */
  STOP
}
