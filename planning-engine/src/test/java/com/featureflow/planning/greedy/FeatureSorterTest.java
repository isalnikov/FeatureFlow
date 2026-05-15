package com.featureflow.planning.greedy;

import com.featureflow.domain.entity.FeatureRequest;
import com.featureflow.domain.entity.Product;
import com.featureflow.domain.exception.DependencyCycleException;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeatureSorterTest {

    @Test
    void sortByPriority_noDependencies_sortedByBusinessValueDesc() {
        var f1 = feature("f1", 30.0);
        var f2 = feature("f2", 50.0);
        var f3 = feature("f3", 10.0);

        List<FeatureRequest> features = List.of(f1, f2, f3);
        List<Product> products = List.of();

        List<FeatureRequest> sorted = FeatureSorter.sortByPriority(features, products);

        assertThat(sorted)
            .extracting(FeatureRequest::getBusinessValue)
            .containsExactly(50.0, 30.0, 10.0);
    }

    @Test
    void sortByPriority_withDependencies_respectsDependencyOrder() {
        var f1 = feature("f1", 30.0);
        var f2 = feature("f2", 50.0);
        var f3 = feature("f3", 10.0);

        f3.addDependency(f2.getId());
        f2.addDependency(f1.getId());

        List<FeatureRequest> features = List.of(f1, f2, f3);
        List<Product> products = List.of();

        List<FeatureRequest> sorted = FeatureSorter.sortByPriority(features, products);

        int f1Idx = indexOf(sorted, f1.getId());
        int f2Idx = indexOf(sorted, f2.getId());
        int f3Idx = indexOf(sorted, f3.getId());

        assertThat(f1Idx).isLessThan(f2Idx);
        assertThat(f2Idx).isLessThan(f3Idx);
    }

    @Test
    void sortByPriority_diamondDependencies_correctTopologicalOrder() {
        var f1 = feature("f1", 10.0);
        var f2 = feature("f2", 20.0);
        var f3 = feature("f3", 30.0);
        var f4 = feature("f4", 40.0);

        f4.addDependency(f2.getId());
        f4.addDependency(f3.getId());
        f2.addDependency(f1.getId());
        f3.addDependency(f1.getId());

        List<FeatureRequest> features = List.of(f1, f2, f3, f4);
        List<Product> products = List.of();

        List<FeatureRequest> sorted = FeatureSorter.sortByPriority(features, products);

        int f1Idx = indexOf(sorted, f1.getId());
        int f2Idx = indexOf(sorted, f2.getId());
        int f3Idx = indexOf(sorted, f3.getId());
        int f4Idx = indexOf(sorted, f4.getId());

        assertThat(f1Idx).isLessThan(f2Idx);
        assertThat(f1Idx).isLessThan(f3Idx);
        assertThat(f2Idx).isLessThan(f4Idx);
        assertThat(f3Idx).isLessThan(f4Idx);
    }

    @Test
    void sortByPriority_cyclicDependency_throwsDependencyCycleException() {
        var f1 = feature("f1", 10.0);
        var f2 = feature("f2", 20.0);
        var f3 = feature("f3", 30.0);

        f1.addDependency(f3.getId());
        f2.addDependency(f1.getId());
        f3.addDependency(f2.getId());

        List<FeatureRequest> features = List.of(f1, f2, f3);
        List<Product> products = List.of();

        assertThatThrownBy(() -> FeatureSorter.sortByPriority(features, products))
            .isInstanceOf(DependencyCycleException.class);
    }

    @Test
    void sortByPriority_samePriority_stableOrder() {
        var f1 = feature("f1", 50.0);
        var f2 = feature("f2", 50.0);
        var f3 = feature("f3", 50.0);

        List<FeatureRequest> features = List.of(f1, f2, f3);
        List<Product> products = List.of();

        List<FeatureRequest> sorted = FeatureSorter.sortByPriority(features, products);

        assertThat(sorted)
            .extracting(FeatureRequest::getId)
            .containsExactlyInAnyOrder(f1.getId(), f2.getId(), f3.getId());
    }

    @Test
    void sortByPriority_mixedDependenciesAndPriority_correctOrder() {
        var f1 = feature("f1", 100.0);
        var f2 = feature("f2", 10.0);
        var f3 = feature("f3", 50.0);

        f2.addDependency(f1.getId());

        List<FeatureRequest> features = List.of(f1, f2, f3);
        List<Product> products = List.of();

        List<FeatureRequest> sorted = FeatureSorter.sortByPriority(features, products);

        int f1Idx = indexOf(sorted, f1.getId());
        int f2Idx = indexOf(sorted, f2.getId());
        int f3Idx = indexOf(sorted, f3.getId());

        assertThat(f1Idx).isLessThan(f2Idx);
        assertThat(f3Idx).isLessThan(f2Idx);
    }

    private static final UUID BASE = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private FeatureRequest feature(String title, double businessValue) {
        UUID id = UUID.nameUUIDFromBytes(title.getBytes());
        return new FeatureRequest(id, title, "", businessValue);
    }

    private int indexOf(List<FeatureRequest> list, UUID id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }
}
