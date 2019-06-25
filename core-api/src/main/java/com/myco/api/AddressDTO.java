package com.myco.api;

import com.myco.api.values.Address;

public class AddressDTO {

  private String street;
  private String city;
  private String state;
  private String zip;
  private String country;

  public String getStreet() {
    return street;
  }

  public AddressDTO setStreet(String street) {
    this.street = street;
    return this;
  }

  public String getCity() {
    return city;
  }

  public AddressDTO setCity(String city) {
    this.city = city;
    return this;
  }

  public String getState() {
    return state;
  }

  public AddressDTO setState(String state) {
    this.state = state;
    return this;
  }

  public String getZip() {
    return zip;
  }

  public AddressDTO setZip(String zip) {
    this.zip = zip;
    return this;
  }

  public String getCountry() {
    return country;
  }

  public AddressDTO setCountry(String country) {
    this.country = country;
    return this;
  }

  public Address toAddressValue() {
    return new Address.Builder().street(getStreet()).city(getCity()).state(getState()).zip(getZip())
        .country(getCountry()).build();
  }
}
