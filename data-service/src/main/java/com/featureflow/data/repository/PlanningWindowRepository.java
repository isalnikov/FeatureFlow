package com.featureflow.data.repository;

import com.featureflow.data.entity.PlanningWindowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PlanningWindowRepository extends JpaRepository<PlanningWindowEntity, UUID> {
}
