package com.myco.order.api;

import javax.validation.constraints.NotNull;

public interface Item {
  @NotNull String getItemId();

  @NotNull String getDescription();

  @NotNull ItemType getType();

  @NotNull ItemFullfillmentStatus getFulfillmentStatus();
}
