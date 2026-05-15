package com.featureflow.data.repository;

import com.featureflow.data.entity.AssignmentEntity;
import com.featureflow.domain.valueobject.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, UUID> {

    @Query("""
        SELECT a FROM AssignmentEntity a
        WHERE a.team.id = :teamId AND a.sprint.id = :sprintId
        AND a.status <> 'COMPLETED'
        """)
    List<AssignmentEntity> findActiveByTeamAndSprint(
        @Param("teamId") UUID teamId,
        @Param("sprintId") UUID sprintId
    );

    @Query("""
        SELECT a FROM AssignmentEntity a
        WHERE a.feature.id = :featureId
        ORDER BY a.sprint.startDate
        """)
    List<AssignmentEntity> findByFeatureIdOrderBySprint(
        @Param("featureId") UUID featureId
    );

    @Query("SELECT a FROM AssignmentEntity a WHERE a.status = 'LOCKED'")
    List<AssignmentEntity> findLockedAssignments();

    List<AssignmentEntity> findByTeamId(UUID teamId);

    List<AssignmentEntity> findBySprintId(UUID sprintId);

    List<AssignmentEntity> findByStatus(AssignmentStatus status);

    @Query("""
        SELECT a FROM AssignmentEntity a
        WHERE a.team.id = :teamId AND a.sprint.id = :sprintId
        """)
    List<AssignmentEntity> findByTeamAndSprint(
        @Param("teamId") UUID teamId,
        @Param("sprintId") UUID sprintId
    );
}
