package com.featureflow.data.repository;

import com.featureflow.data.entity.TeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<TeamEntity, UUID> {

    @Query(value = """
        SELECT t.* FROM teams t
        JOIN product_teams pt ON t.id = pt.team_id
        WHERE pt.product_id = :productId
        """, nativeQuery = true)
    List<TeamEntity> findByProductId(@Param("productId") UUID productId);
}
