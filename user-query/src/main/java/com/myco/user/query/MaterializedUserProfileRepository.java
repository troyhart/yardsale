package com.myco.user.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterializedUserProfileRepository extends JpaRepository<MaterializedUserProfile, String> {

}
