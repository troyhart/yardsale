package com.myco.api;

import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum Color implements LabeledAttribute {
  UNKNOWN("UNKNOWN", true), BLACK("Black"), BLUE("Blue"), BROWN("Brown"), CLEAR_TRANSPARENT("Clear/Transparent"), GRAY(
      "Gray"), GREEN("Green"), ORANGE("Orange"), PINK("Pink"), PURPLE("Purple"), RED("Red"), SILVER("Silver"), TAN_BUFF(
      "Tan/Buff"), WHITE("White"), YELLOW("Yellow"), GOLD("Gold"), COPPER("Copper");

  private String label;
  private boolean deprecated;

  Color(String label) {
    Assert.hasText(label, "null/blank label");
    this.label = label;
  }

  Color(String label, boolean deprecated) {
    this(label);
    Assert
        .isTrue(deprecated, "Invalid usage; this constructor is intended only for characteristics that are deprecated");
    this.deprecated = deprecated;
  }

  public static Map<String, String> nameToLabelMapping() {
    return Arrays.stream(values()).collect(Collectors.toMap(Color::name, Color::label)).entrySet().stream()
        .sorted(Map.Entry.<String, String>comparingByValue()).collect(Collectors
            .toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
  }

  public static void main(String[] args) {
    System.out.println("******** Name to Label Mapping For Colors: ");
    System.out.println(nameToLabelMapping());
  }

  @Override
  public String label() {
    return label;
  }

  @Override
  public boolean deprecated() {
    return deprecated;
  }
}
