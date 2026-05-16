package com.featureflow.data.service;

import com.featureflow.data.dto.*;
import com.featureflow.data.entity.*;
import com.featureflow.data.mapper.EntityMapper;
import com.featureflow.data.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private ProductEntity productEntity;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        productEntity = new ProductEntity();
        productEntity.setId(productId);
        productEntity.setName("Test Product");
        productEntity.setDescription("A test product");
        productEntity.setTechnologyStack(new HashSet<>(List.of("java", "spring")));
        productEntity.setVersion(1);

        productDto = new ProductDto(
            productId,
            "Test Product",
            "A test product",
            new HashSet<>(List.of("java", "spring")),
            1,
            null, null, null, null
        );
    }

    @Test
    void listAll_shouldReturnProducts() {
        when(productRepository.findAll()).thenReturn(List.of(productEntity));
        when(mapper.toProductDto(productEntity)).thenReturn(productDto);

        List<ProductDto> result = productService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Test Product");
    }

    @Test
    void getById_shouldReturnProduct() {
        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(mapper.toProductDto(productEntity)).thenReturn(productDto);

        ProductDto result = productService.getById(productId);

        assertThat(result.id()).isEqualTo(productId);
    }

    @Test
    void getById_notFound_shouldThrow() {
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getById(productId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(productId.toString());
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        CreateProductRequest request = new CreateProductRequest("New Product", "Description", Set.of("java"));

        when(mapper.toProductEntity(request)).thenReturn(productEntity);
        when(productRepository.save(productEntity)).thenReturn(productEntity);
        when(mapper.toProductDto(productEntity)).thenReturn(productDto);

        ProductDto result = productService.create(request);

        assertThat(result).isEqualTo(productDto);
        verify(productRepository).save(productEntity);
    }

    @Test
    void update_shouldModifyAndReturnDto() {
        UpdateProductRequest request = new UpdateProductRequest("Updated Name", null, null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(productRepository.save(productEntity)).thenReturn(productEntity);
        when(mapper.toProductDto(productEntity)).thenReturn(productDto);

        ProductDto result = productService.update(productId, request);

        assertThat(productEntity.getName()).isEqualTo("Updated Name");
        assertThat(result).isEqualTo(productDto);
    }

    @Test
    void update_notFound_shouldThrow() {
        UpdateProductRequest request = new UpdateProductRequest("Updated Name", null, null);
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(productId, request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_shouldRemoveProduct() {
        when(productRepository.existsById(productId)).thenReturn(true);

        productService.delete(productId);

        verify(productRepository).deleteById(productId);
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(productId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getTeams_shouldReturnTeams() {
        TeamEntity teamEntity = new TeamEntity();
        teamEntity.setId(UUID.randomUUID());
        teamEntity.setName("Team Alpha");

        TeamDto teamDto = new TeamDto(
            teamEntity.getId(),
            "Team Alpha",
            null, null, null, null, null, null, null, null, null, null, null
        );

        when(productRepository.existsById(productId)).thenReturn(true);
        when(teamRepository.findByProductId(productId)).thenReturn(List.of(teamEntity));
        when(mapper.toTeamDto(teamEntity)).thenReturn(teamDto);

        List<TeamDto> result = productService.getTeams(productId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Team Alpha");
    }

    @Test
    void getTeams_productNotFound_shouldThrow() {
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThatThrownBy(() -> productService.getTeams(productId))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
