package com.myco.api;

public interface LabeledAttribute extends Labeled {
  String name();

  boolean deprecated();
}
