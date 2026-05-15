package com.featureflow.data.repository;

import com.featureflow.data.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {
}
