package com.featureflow.data.repository;

import com.featureflow.data.entity.SprintEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SprintRepository extends JpaRepository<SprintEntity, UUID> {

    List<SprintEntity> findByPlanningWindowId(UUID planningWindowId);

    boolean existsByExternalId(String externalId);
}
