package com.myco.api;

import java.util.Comparator;

public interface Labeled {

  public static final Comparator<Labeled> COMPARATOR = new Comparator<Labeled>() {
    @Override
    public int compare(Labeled o1, Labeled o2) {
      if (o1 == o2) {
        return 0;
      }

      if (o1 == null) {
        return -1;
      }

      if (o2 == null) {
        return 1;
      }

      if (o1.label() == o2.label()) {
        return 0;
      }

      if (o1.label() == null) {
        return -1;
      }

      if (o2.label() == null) {
        return 1;
      }

      return o1.label().compareTo(o2.label());
    }
  };

  String label();
}
