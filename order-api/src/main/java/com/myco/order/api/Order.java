package com.myco.order.api;

import com.myco.api.values.Money;

import java.util.List;

public interface Order {
  String getOrderId();

  String getUserId();

  List<String> getStockItemIds();

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
