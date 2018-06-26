package de.uka.ipd.sdq.pcm.transformations.builder.util;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourcetype.ResourceRepository;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMModelHolder {
    private Repository repository;
    private System system;
    private Allocation allocation;
    private ResourceEnvironment resourceEnvironment;
    private UsageModel usageModel;
    private ResourceRepository resourceRepository;

    public Repository getRepository() {
        return repository;
    }

    public System getSystem() {
        return system;
    }

    public Allocation getAllocation() {
        return allocation;
    }

    public ResourceEnvironment getResourceEnvironment() {
        return resourceEnvironment;
    }

    public UsageModel getUsageModel() {
        return usageModel;
    }

    public PCMModelHolder(ResourceRepository resourceRepository, Repository repository, System system,
            Allocation allocation, UsageModel usageModel) {
        super();
        this.resourceRepository = resourceRepository;
        this.repository = repository;
        this.system = system;
        this.allocation = allocation;
        this.resourceEnvironment = allocation.getTargetResourceEnvironment_Allocation();
        this.usageModel = usageModel;
    }

    public ResourceRepository getResourceRepository() {
        return resourceRepository;
    }
}
