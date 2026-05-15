package com.featureflow.data.repository;

import com.featureflow.data.entity.FeatureEntity;
import com.featureflow.domain.valueobject.ClassOfService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FeatureRepository extends JpaRepository<FeatureEntity, UUID> {

    Page<FeatureEntity> findByClassOfService(ClassOfService classOfService, Pageable pageable);

    @Query("SELECT f FROM FeatureEntity f WHERE f.deadline <= :date AND f.deadline IS NOT NULL")
    List<FeatureEntity> findFeaturesWithDeadlineBefore(@Param("date") LocalDate date);

    @Query(value = """
        SELECT f.* FROM features f
        JOIN feature_products fp ON f.id = fp.feature_id
        WHERE fp.product_id = :productId
        """, nativeQuery = true)
    List<FeatureEntity> findByProductId(@Param("productId") UUID productId);

    @Query("SELECT f FROM FeatureEntity f LEFT JOIN FETCH f.products WHERE f.id = :id")
    FeatureEntity findByIdWithProducts(@Param("id") UUID id);

    @Query("SELECT f FROM FeatureEntity f LEFT JOIN FETCH f.dependencies WHERE f.id = :id")
    FeatureEntity findByIdWithDependencies(@Param("id") UUID id);
}
