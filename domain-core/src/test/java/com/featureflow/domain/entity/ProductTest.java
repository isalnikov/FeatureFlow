package com.featureflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    void create_shouldInitializeDefaults() {
        var product = new Product(UUID.randomUUID(), "Test Product", "Description");

        assertThat(product.getTechnologyStack()).isEmpty();
        assertThat(product.getTeamIds()).isEmpty();
    }

    @Test
    void addTechnology_shouldAddToSet() {
        var product = new Product(UUID.randomUUID(), "Test", "Desc");
        product.addTechnology("Java");
        assertThat(product.getTechnologyStack()).contains("Java");
    }

    @Test
    void removeTechnology_shouldRemoveFromSet() {
        var product = new Product(UUID.randomUUID(), "Test", "Desc");
        product.addTechnology("Java");
        product.removeTechnology("Java");
        assertThat(product.getTechnologyStack()).doesNotContain("Java");
    }

    @Test
    void addTeamId_shouldAddToSet() {
        var product = new Product(UUID.randomUUID(), "Test", "Desc");
        UUID teamId = UUID.randomUUID();
        product.addTeamId(teamId);
        assertThat(product.getTeamIds()).contains(teamId);
    }

    @Test
    void isOwnedBy_shouldCheckTeamOwnership() {
        var product = new Product(UUID.randomUUID(), "Test", "Desc");
        UUID teamId = UUID.randomUUID();
        product.addTeamId(teamId);

        assertThat(product.isOwnedBy(teamId)).isTrue();
        assertThat(product.isOwnedBy(UUID.randomUUID())).isFalse();
    }

    @Test
    void equality_shouldBeBasedOnId() {
        UUID id = UUID.randomUUID();
        var p1 = new Product(id, "Product 1", "Desc");
        var p2 = new Product(id, "Product 2", "Other");

        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }
}
