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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureServiceTest {

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private EntityMapper mapper;

    @InjectMocks
    private FeatureService featureService;

    private UUID featureId;
    private FeatureEntity featureEntity;
    private FeatureDto featureDto;

    @BeforeEach
    void setUp() {
        featureId = UUID.randomUUID();

        featureEntity = new FeatureEntity();
        featureEntity.setId(featureId);
        featureEntity.setTitle("Test Feature");
        featureEntity.setDescription("A test feature");
        featureEntity.setBusinessValue(50.0);
        featureEntity.setDeadline(LocalDate.of(2025, 6, 1));
        featureEntity.setCanSplit(false);
        featureEntity.setVersion(1);

        featureDto = new FeatureDto();
        featureDto.setId(featureId);
        featureDto.setTitle("Test Feature");
        featureDto.setDescription("A test feature");
        featureDto.setBusinessValue(50.0);
        featureDto.setDeadline(LocalDate.of(2025, 6, 1));
        featureDto.setCanSplit(false);
        featureDto.setVersion(1);
    }

    @Test
    void listAll_shouldReturnFeatures() {
        when(featureRepository.findAll()).thenReturn(List.of(featureEntity));
        when(mapper.toFeatureDto(featureEntity)).thenReturn(featureDto);

        List<FeatureDto> result = featureService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Feature");
    }

    @Test
    void listAll_withPagination_shouldReturnPage() {
        Page<FeatureEntity> page = new PageImpl<>(List.of(featureEntity));
        when(featureRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(mapper.toFeatureDto(featureEntity)).thenReturn(featureDto);

        Page<FeatureDto> result = featureService.listAll(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getById_shouldReturnFeature() {
        when(featureRepository.findById(featureId)).thenReturn(Optional.of(featureEntity));
        when(mapper.toFeatureDto(featureEntity)).thenReturn(featureDto);

        FeatureDto result = featureService.getById(featureId);

        assertThat(result.getId()).isEqualTo(featureId);
    }

    @Test
    void getById_notFound_shouldThrow() {
        when(featureRepository.findById(featureId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> featureService.getById(featureId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(featureId.toString());
    }

    @Test
    void create_shouldSaveAndReturnDto() {
        CreateFeatureRequest request = new CreateFeatureRequest();
        request.setTitle("New Feature");
        request.setBusinessValue(75.0);

        when(mapper.toFeatureEntity(request)).thenReturn(featureEntity);
        when(featureRepository.save(featureEntity)).thenReturn(featureEntity);
        when(mapper.toFeatureDto(featureEntity)).thenReturn(featureDto);

        FeatureDto result = featureService.create(request);

        assertThat(result).isEqualTo(featureDto);
        verify(featureRepository).save(featureEntity);
    }

    @Test
    void create_withProductIds_shouldLinkProducts() {
        CreateFeatureRequest request = new CreateFeatureRequest();
        request.setTitle("New Feature");
        request.setProductIds(Set.of(UUID.randomUUID()));

        ProductEntity product = new ProductEntity();
        product.setId(UUID.randomUUID());

        when(mapper.toFeatureEntity(request)).thenReturn(featureEntity);
        when(productRepository.findAllById(request.getProductIds())).thenReturn(List.of(product));
        when(featureRepository.save(featureEntity)).thenReturn(featureEntity);
        when(mapper.toFeatureDto(featureEntity)).thenReturn(featureDto);

        FeatureDto result = featureService.create(request);

        assertThat(featureEntity.getProducts()).contains(product);
        assertThat(result).isEqualTo(featureDto);
    }

    @Test
    void update_shouldModifyAndReturnDto() {
        UpdateFeatureRequest request = new UpdateFeatureRequest();
        request.setTitle("Updated Title");
        request.setBusinessValue(80.0);

        when(featureRepository.findById(featureId)).thenReturn(Optional.of(featureEntity));
        when(featureRepository.save(featureEntity)).thenReturn(featureEntity);
        when(mapper.toFeatureDto(featureEntity)).thenReturn(featureDto);

        FeatureDto result = featureService.update(featureId, request);

        assertThat(featureEntity.getTitle()).isEqualTo("Updated Title");
        assertThat(featureEntity.getBusinessValue()).isEqualTo(80.0);
        assertThat(result).isEqualTo(featureDto);
    }

    @Test
    void update_notFound_shouldThrow() {
        UpdateFeatureRequest request = new UpdateFeatureRequest();
        when(featureRepository.findById(featureId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> featureService.update(featureId, request))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_shouldRemoveFeature() {
        when(featureRepository.existsById(featureId)).thenReturn(true);

        featureService.delete(featureId);

        verify(featureRepository).deleteById(featureId);
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(featureRepository.existsById(featureId)).thenReturn(false);

        assertThatThrownBy(() -> featureService.delete(featureId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void bulkCreate_shouldSaveAllAndReturnDtos() {
        CreateFeatureRequest req1 = new CreateFeatureRequest();
        req1.setTitle("Feature 1");
        CreateFeatureRequest req2 = new CreateFeatureRequest();
        req2.setTitle("Feature 2");

        FeatureEntity entity2 = new FeatureEntity();
        entity2.setId(UUID.randomUUID());
        entity2.setTitle("Feature 2");

        FeatureDto dto2 = new FeatureDto();
        dto2.setId(entity2.getId());
        dto2.setTitle("Feature 2");

        when(mapper.toFeatureEntity(req1)).thenReturn(featureEntity);
        when(mapper.toFeatureEntity(req2)).thenReturn(entity2);
        when(featureRepository.saveAll(any())).thenReturn(List.of(featureEntity, entity2));
        when(mapper.toFeatureDto(featureEntity)).thenReturn(featureDto);
        when(mapper.toFeatureDto(entity2)).thenReturn(dto2);

        List<FeatureDto> result = featureService.bulkCreate(List.of(req1, req2));

        assertThat(result).hasSize(2);
        verify(featureRepository).saveAll(any());
    }

    @Test
    void getDependencies_shouldReturnDependencyIds() {
        FeatureEntity depEntity = new FeatureEntity();
        depEntity.setId(UUID.randomUUID());
        featureEntity.setDependencies(Set.of(depEntity));

        when(featureRepository.findByIdWithDependencies(featureId)).thenReturn(featureEntity);

        Set<UUID> result = featureService.getDependencies(featureId);

        assertThat(result).containsExactly(depEntity.getId());
    }

    @Test
    void getDependencies_notFound_shouldThrow() {
        when(featureRepository.findByIdWithDependencies(featureId)).thenReturn(null);

        assertThatThrownBy(() -> featureService.getDependencies(featureId))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateDependencies_shouldSetDependencies() {
        UUID depId = UUID.randomUUID();
        FeatureEntity depEntity = new FeatureEntity();
        depEntity.setId(depId);

        when(featureRepository.findByIdWithDependencies(featureId)).thenReturn(featureEntity);
        when(featureRepository.findAllById(Set.of(depId))).thenReturn(List.of(depEntity));

        featureService.updateDependencies(featureId, Set.of(depId));

        assertThat(featureEntity.getDependencies()).contains(depEntity);
        verify(featureRepository).save(featureEntity);
    }

    @Test
    void updateDependencies_notFound_shouldThrow() {
        when(featureRepository.findByIdWithDependencies(featureId)).thenReturn(null);

        assertThatThrownBy(() -> featureService.updateDependencies(featureId, Set.of()))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
