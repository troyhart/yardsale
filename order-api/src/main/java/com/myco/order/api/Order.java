package com.myco.order.api;

import com.myco.api.values.Money;

import java.util.Set;

public interface Order {
  String getOrderId();

  String getUserId();

  Set<Item> getItems();

  OrderStatus getOrderStatus();

  Money getItemTotal();

  Money getDiscounts();

  default boolean hasDiscounts() {
    return getDiscounts() != null;
  }

  default Money getOrderTotal() {
    return hasDiscounts() ? getItemTotal().subtract(getDiscounts()) : getItemTotal();
  }
}
