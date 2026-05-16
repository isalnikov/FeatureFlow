package com.featureflow.data.service;

import com.featureflow.data.dto.CreateProductRequest;
import com.featureflow.data.dto.ProductDto;
import com.featureflow.data.dto.TeamDto;
import com.featureflow.data.dto.UpdateProductRequest;
import com.featureflow.data.entity.ProductEntity;
import com.featureflow.data.mapper.EntityMapper;
import com.featureflow.data.repository.ProductRepository;
import com.featureflow.data.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final EntityMapper mapper;

    public ProductService(ProductRepository productRepository,
                          TeamRepository teamRepository,
                          EntityMapper mapper) {
        this.productRepository = productRepository;
        this.teamRepository = teamRepository;
        this.mapper = mapper;
    }

    public List<ProductDto> listAll() {
        return productRepository.findAll().stream()
                .map(mapper::toProductDto)
                .collect(Collectors.toList());
    }

    public ProductDto getById(UUID id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        return mapper.toProductDto(entity);
    }

    @Transactional
    public ProductDto create(CreateProductRequest request) {
        ProductEntity entity = mapper.toProductEntity(request);
        entity = productRepository.save(entity);
        return mapper.toProductDto(entity);
    }

    @Transactional
    public ProductDto update(UUID id, UpdateProductRequest request) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.description() != null) {
            entity.setDescription(request.description());
        }
        if (request.technologyStack() != null) {
            entity.setTechnologyStack(request.technologyStack());
        }
        entity = productRepository.save(entity);
        return mapper.toProductDto(entity);
    }

    @Transactional
    public void delete(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public List<TeamDto> getTeams(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Product not found with id: " + productId);
        }
        return teamRepository.findByProductId(productId).stream()
                .map(mapper::toTeamDto)
                .collect(Collectors.toList());
    }
}
