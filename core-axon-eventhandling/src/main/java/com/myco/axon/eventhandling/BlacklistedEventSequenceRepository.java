package com.myco.axon.eventhandling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedEventSequenceRepository extends JpaRepository<BlacklistedEventSequence, String> {
}
