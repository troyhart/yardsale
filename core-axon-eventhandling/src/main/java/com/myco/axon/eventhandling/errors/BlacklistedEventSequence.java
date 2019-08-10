package com.myco.axon.eventhandling.errors;

import javax.persistence.Column;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.Objects;

@Entity
public class BlacklistedEventSequence {

  @Id
  private String id;
  @Column
  private Instant createdInstant;

  public BlacklistedEventSequence() {
  }

  public BlacklistedEventSequence(String id) {
    this.id = id;
    createdInstant = Instant.now();
  }

  public String getId() {
    return id;
  }

  public Instant getCreatedInstant() {
    return createdInstant;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof BlacklistedEventSequence)) return false;
    BlacklistedEventSequence that = (BlacklistedEventSequence) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
  @Override
  public String toString() {
    return "BlacklistedEventSequence{" + "id='" + id + '\'' + ", createdInstant=" + createdInstant + '}';
  }
}
