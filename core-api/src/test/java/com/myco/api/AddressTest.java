package com.myco.api;

import com.myco.api.values.Address;
import com.myco.util.v8n.V8NException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class AddressTest {

  @Before
  public void setUp() {
  }

  @Test
  public void basicValidationTest() {
    String street = "My Street";
    String city = "My City";
    String state = "ST";
    String zip = "11111";
    String country = "USA";

    // Verify Street is validated
    try {
      new Address(null, city, state, zip, country);
      fail("Expecting IllegalArgumentException.........");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    try {
      Address addr = new Address(" ", city, state, zip, country);
      fail("Expecting V8NException.........");
    }
    catch (V8NException e) {
      // expected
    }

    // Verify city is validated
    try {
      new Address(street, null, state, zip, country);
      fail("Expecting IllegalArgumentException.........");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    try {
      Address addr = new Address(street, " ", state, zip, country);
      fail("Expecting V8NException.........");
    }
    catch (V8NException e) {
      // expected
    }

    // Verify state is validated
    try {
      new Address(street, city, null, zip, country);
      fail("Expecting IllegalArgumentException.........");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    try {
      Address addr = new Address(street, city, " ", zip, country);
      fail("Expecting V8NException.........");
    }
    catch (V8NException e) {
      // expected
    }

    // Verify zip is validated
    try {
      new Address(street, city, state, null, country);
      fail("Expecting IllegalArgumentException.........");
    }
    catch (IllegalArgumentException e) {
      // expected
    }

    try {
      Address addr = new Address(street, city, state, " ", country);
      fail("Expecting V8NException.........");
    }
    catch (V8NException e) {
      // expected
    }

    // Verify country is validated
    try {
      Address addr = new Address(street, city, state, zip, null);
      fail("Expecting V8NException.........");
    }
    catch (V8NException e) {
      // expected
    }

    try {
      Address addr = new Address(street, city, state, zip, " ");
      fail("Expecting V8NException.........");
    }
    catch (V8NException e) {
      // expected
    }
  }

}
