package com.myco.order.api;

import com.myco.api.values.Money;

import java.util.List;

public interface Order {
  List<String> getStockedItemIds();
  Money orderTotal();
}
