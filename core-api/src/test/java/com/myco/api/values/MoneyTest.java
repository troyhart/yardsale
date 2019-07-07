package com.myco.api.values;

import com.myco.util.v8n.V8NException;
import org.junit.Assert;
import org.junit.Test;

import java.util.Currency;


public class MoneyTest {

  @Test
  public void test0() {
    Money m1 = new Money.Builder().value("12.4999").currency(Currency.getInstance("USD")).build();
    Money m2 = new Money.Builder().value("25").currency(Currency.getInstance("USD")).build();
    Money result = m1.add(m2);
    Assert.assertEquals("37.50", result.getValue());
  }

  @Test
  public void test1() {
    Money m1 = new Money.Builder().value("12.4999").build();// USD by default
    Money m2 = new Money.Builder().value("25").currency(Currency.getInstance("USD")).build();
    Money result = m1.add(m2);
    Assert.assertEquals("37.50", result.getValue());
  }

  @Test(expected = V8NException.class)
  public void test2() {
    Money m1 = new Money.Builder().value("12.4999").currency(Currency.getInstance("USD")).build();
    Money m2 = new Money.Builder().value("25").currency(Currency.getInstance("JPY")).build();
    m1.add(m2);
  }

  @Test(expected = V8NException.class)
  public void test3() {
    Money m1 = new Money.Builder().value("12x4999").build();
  }

  @Test(expected = V8NException.class)
  public void test4() {
    Money m1 = new Money("12x4999", Currency.getInstance("USD"));
  }
}
