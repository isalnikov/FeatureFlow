package com.featureflow.data.service;

import com.featureflow.data.dto.CreateFeatureRequest;
import com.featureflow.data.dto.FeatureDto;
import com.featureflow.data.dto.UpdateFeatureRequest;
import com.featureflow.data.entity.FeatureEntity;
import com.featureflow.data.entity.ProductEntity;
import com.featureflow.data.mapper.EntityMapper;
import com.featureflow.data.repository.FeatureRepository;
import com.featureflow.data.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FeatureService {

    private final FeatureRepository featureRepository;
    private final ProductRepository productRepository;
    private final EntityMapper mapper;

    public FeatureService(FeatureRepository featureRepository,
                          ProductRepository productRepository,
                          EntityMapper mapper) {
        this.featureRepository = featureRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
    }

    public List<FeatureDto> listAll() {
        return featureRepository.findAll().stream()
                .map(mapper::toFeatureDto)
                .collect(Collectors.toList());
    }

    public Page<FeatureDto> listAll(Pageable pageable) {
        return featureRepository.findAll(pageable).map(mapper::toFeatureDto);
    }

    public FeatureDto getById(UUID id) {
        FeatureEntity entity = featureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found with id: " + id));
        return mapper.toFeatureDto(entity);
    }

    @Transactional
    public FeatureDto create(CreateFeatureRequest request) {
        FeatureEntity entity = mapper.toFeatureEntity(request);
        if (request.productIds() != null && !request.productIds().isEmpty()) {
            Set<ProductEntity> products = new HashSet<>(productRepository.findAllById(request.productIds()));
            entity.setProducts(products);
        }
        entity = featureRepository.save(entity);
        return mapper.toFeatureDto(entity);
    }

    @Transactional
    public FeatureDto update(UUID id, UpdateFeatureRequest request) {
        FeatureEntity entity = featureRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feature not found with id: " + id));
        if (request.title() != null) {
            entity.setTitle(request.title());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.businessValue() != null) {
            entity.setBusinessValue(request.businessValue());
        }
        if (request.deadline() != null) {
            entity.setDeadline(request.deadline());
        }
        if (request.classOfService() != null) {
            entity.setClassOfService(request.classOfService());
        }
        if (request.effortEstimate() != null) {
            entity.setEffortEstimate(request.effortEstimate());
        }
        if (request.stochasticEstimate() != null) {
            entity.setStochasticEstimate(request.stochasticEstimate());
        }
        if (request.requiredExpertise() != null) {
            entity.setRequiredExpertise(request.requiredExpertise());
        }
        if (request.canSplit() != null) {
            entity.setCanSplit(request.canSplit());
        }
        entity = featureRepository.save(entity);
        return mapper.toFeatureDto(entity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!featureRepository.existsById(id)) {
            throw new EntityNotFoundException("Feature not found with id: " + id);
        }
        featureRepository.deleteById(id);
    }

    @Transactional
    public List<FeatureDto> bulkCreate(List<CreateFeatureRequest> requests) {
        List<FeatureEntity> entities = requests.stream()
                .map(mapper::toFeatureEntity)
                .collect(Collectors.toList());
        entities = featureRepository.saveAll(entities);
        return entities.stream()
                .map(mapper::toFeatureDto)
                .collect(Collectors.toList());
    }

    public Set<UUID> getDependencies(UUID featureId) {
        FeatureEntity entity = featureRepository.findByIdWithDependencies(featureId);
        if (entity == null) {
            throw new EntityNotFoundException("Feature not found with id: " + featureId);
        }
        return entity.getDependencies().stream()
                .map(FeatureEntity::getId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void updateDependencies(UUID featureId, Set<UUID> dependencies) {
        FeatureEntity entity = featureRepository.findByIdWithDependencies(featureId);
        if (entity == null) {
            throw new EntityNotFoundException("Feature not found with id: " + featureId);
        }
        Set<FeatureEntity> dependencyEntities = new HashSet<>(featureRepository.findAllById(dependencies));
        entity.setDependencies(dependencyEntities);
        featureRepository.save(entity);
    }
}
