package com.myco.item.api;

import java.util.Set;

public interface Catalog {
  String getCatalogId();

  String getName();

  Set<String> getItemIds();
}
