package de.uka.ipd.sdq.pcm.transformations.builder.util;

import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.resourcetype.ResourceRepository;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMAndCompletionModelHolder extends PCMModelHolder {

    private Repository completionRepository;
    private Repository middlewareRepository;

    public PCMAndCompletionModelHolder(ResourceRepository resourceRepository, Repository repository, System system,
            Allocation allocation, UsageModel usageModel, Repository completionRepository,
            Repository middlewareRepository) {
        super(resourceRepository, repository, system, allocation, usageModel);
        this.completionRepository = completionRepository;
        this.middlewareRepository = middlewareRepository;
    }

    public Repository getCompletionRepository() {
        return completionRepository;
    }

    public Repository getMiddlewareRepository() {
        return middlewareRepository;
    }

}
