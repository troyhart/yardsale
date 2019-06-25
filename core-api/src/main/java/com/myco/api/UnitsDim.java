package com.myco.api;

public enum UnitsDim implements Labeled {
  IN("inches"), FT("feet"), MM("milimeter"), CM("centimeter"), M("meter");

  private String label;

  private UnitsDim(String label) {
    this.label = label;
  }

  @Override
  public String label() {
    return label;
  }

  public boolean isMetric() {
    return this == MM || this == CM || this == M;
  }

  /**
   * @param value
   * @param from
   * @param to
   *
   * @return the converted value
   */
  public double convert(double value, UnitsDim from, UnitsDim to) {
    // TODO: implement me
    throw new RuntimeException("Method not implemented");
  }
}
