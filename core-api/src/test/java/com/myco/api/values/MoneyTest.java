package com.myco.api.values;

import com.myco.util.v8n.V8NException;
import org.junit.Assert;
import org.junit.Test;

import java.text.NumberFormat;
import java.util.Currency;


public class MoneyTest {

  static final NumberFormat USD_CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
  static {
    USD_CURRENCY_FORMAT.setMinimumFractionDigits(Currency.getInstance("USD").getDefaultFractionDigits());
    USD_CURRENCY_FORMAT.setMaximumFractionDigits(Currency.getInstance("USD").getDefaultFractionDigits());
  }

  @Test(expected = V8NException.class) public void testCantAddMixedCurrencies() {
    Money m1 = new Money.Builder().amount("12.4999").currency(Currency.getInstance("USD")).build();
    Money m2 = new Money.Builder().amount(25).currency(Currency.getInstance("JPY")).build();
    m1.add(m2);
    System.out.println(String
        .format("OOOOPS! I was expecting a validation exception for providing monies with mixed currencies: %s AND %s", m1.toString(USD_CURRENCY_FORMAT), m2.toString(USD_CURRENCY_FORMAT)));
  }

  @Test(expected = NumberFormatException.class) public void testCantBuildMoneyWithNonNumericAmount_12x4999() {
    new Money.Builder().amount("12x4999").build();
  }

  @Test(expected = NumberFormatException.class) public void testCantBuildMoneyWithNonNumericAmount__100() {
    new Money.Builder().amount("_100").build();
  }

  @Test public void testCantBuildMoneyWithCommasInAmount() {
    new Money.Builder().amount("1,000").build();
  }

  @Test public void testCanBuildMoneyWithDollarSignPrefixedAmount() {
    new Money.Builder().amount("$100").build();
  }

  @Test public void testCanBuildMoneyWithDecimalPointInAmount() {
    new Money.Builder().amount("100.01").build();
  }

  @Test public void testBuilderWithIntAmount() {
    Money m1 = new Money.Builder().amount("12.4999").build();
    Money m2 = new Money.Builder().amount(25).build();
    Money expected = new Money.Builder().amount("37.50").build();
    Assert.assertEquals(expected, m1.add(m2));
    System.out.println(String.format("%s + %s == %s", m1.toString(USD_CURRENCY_FORMAT), m2.toString(USD_CURRENCY_FORMAT), expected.toString(USD_CURRENCY_FORMAT)));
  }

  @Test public void testBuilderWithNegativeIntAmount() {
    Money m1 = new Money.Builder().amount("12.4999").build();
    Money m2 = new Money.Builder().amount(-25).build();
    Money expected = new Money.Builder().amount("-12.50").build();
    Assert.assertEquals(expected, m1.add(m2));
    System.out.println(String.format("%s + %s == %s", m1.toString(USD_CURRENCY_FORMAT), m2.toString(USD_CURRENCY_FORMAT), expected.toString(USD_CURRENCY_FORMAT)));
  }

  @Test public void testSubtract() {
    Money m1 = new Money.Builder().amount("12.5").build();
    Money m2 = new Money.Builder().amount(25).build();
    Money expected = new Money.Builder().amount("-12.50").build();
    Assert.assertEquals(expected, m1.subtract(m2));
    System.out.println(String.format("%s - %s == %s", m1.toString(USD_CURRENCY_FORMAT), m2.toString(USD_CURRENCY_FORMAT), expected.toString(USD_CURRENCY_FORMAT)));
  }
}
