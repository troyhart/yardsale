package com.myco.item.api;


import com.myco.api.Color;
import com.myco.api.values.Weight;

import java.util.Map;
import java.util.Set;

public interface Item {
  String getItemId();

  String getUPC();

  String getMfrName();

  String getMfrPartNo();

  String getBrand();

  String getDescription();

  String getCategory();

  Set<Color> getColors();

  Weight getWeight();

  Map<String, String> getOtherAttributes();
}
