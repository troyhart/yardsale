package com.myco.api;

import com.myco.api.values.Address;
import com.myco.util.values.ErrorMessage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
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
      // expected.....
    }
    {
      Address addr = new Address(" ", city, state, zip, country);
      ErrorMessage emsg = addr.validate();
      assertNotNull("expecting validation error", emsg);
    }

    // Verify city is validated
    try {
      new Address(street, null, state, zip, country);
      fail("Expecting IllegalArgumentException.........");
    }
    catch (IllegalArgumentException e) {
      // expected.....
    }
    {
      Address addr = new Address(street, " ", state, zip, country);
      ErrorMessage emsg = addr.validate();
      assertNotNull("expecting validation error", emsg);
    }

    // Verify state is validated
    try {
      new Address(street, city, null, zip, country);
      fail("Expecting IllegalArgumentException.........");
    }
    catch (IllegalArgumentException e) {
      // expected.....
    }
    {
      Address addr = new Address(street, city, " ", zip, country);
      ErrorMessage emsg = addr.validate();
      assertNotNull("expecting validation error", emsg);
    }

    // Verify zip is validated
    try {
      new Address(street, city, state, null, country);
      fail("Expecting IllegalArgumentException.........");
    }
    catch (IllegalArgumentException e) {
      // expected.....
    }
    {
      Address addr = new Address(street, city, state, " ", country);
      ErrorMessage emsg = addr.validate();
      assertNotNull("expecting validation error", emsg);
    }

    // Verify country is validated
    {
      Address addr = new Address(street, city, state, zip, null);
      ErrorMessage emsg = addr.validate();
      assertNotNull("expecting validation error", emsg);
    }
    {
      Address addr = new Address(street, city, state, zip, " ");
      ErrorMessage emsg = addr.validate();
      assertNotNull("expecting validation error", emsg);
    }
  }

}
