package com.myco.stock.api;


import com.myco.api.values.Money;

import java.util.Map;

public interface Item {
  String getStockItemId();

  String getItemId();

  String getLocationId();

  Money getPrice();

  Map<String, String> getOtherAttributes();
}
