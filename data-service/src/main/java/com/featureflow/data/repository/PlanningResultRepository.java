package com.featureflow.data.repository;

import com.featureflow.data.entity.PlanningResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;

public interface PlanningResultRepository extends JpaRepository<PlanningResultEntity, UUID> {

    @Query("SELECT p FROM PlanningResultEntity p WHERE (:algorithm IS NULL OR p.algorithm = :algorithm) ORDER BY p.createdAt DESC")
    Page<PlanningResultEntity> findRecent(@Param("algorithm") String algorithm, Pageable pageable);
}
