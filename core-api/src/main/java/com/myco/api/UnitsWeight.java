package com.myco.api;

public enum UnitsWeight implements Labeled {
  LBS("pounds"), KG("kilograms");

  private String label;

  private UnitsWeight(String label) {
    this.label = label;
  }

  @Override
  public String label() {
    return label;
  }

  public boolean isMetric() {
    return this == KG;
  }

  /**
   * @param value
   * @param from
   * @param to
   * @return the converted value
   */
  public double convert(double value, UnitsWeight from, UnitsWeight to) {
    // TODO: implement me
    throw new RuntimeException("Method not implemented");
  }
}
