package com.featureflow.data.dto;

import java.util.Set;

public class CreateProductRequest {
    private String name;
    private String description;
    private Set<String> technologyStack;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<String> getTechnologyStack() { return technologyStack; }
    public void setTechnologyStack(Set<String> technologyStack) { this.technologyStack = technologyStack; }
}
