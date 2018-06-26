package de.uka.ipd.sdq.pcm.transformations.builder.seff;

import org.palladiosimulator.analyzer.completions.CompletionsFactory;
import org.palladiosimulator.analyzer.completions.NetworkDemandParametricResourceDemand;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.resourcetype.CommunicationLinkResourceType;
import org.palladiosimulator.pcm.resourcetype.ProcessingResourceType;
import org.palladiosimulator.pcm.resourcetype.ResourceRepository;
import org.palladiosimulator.pcm.resourcetype.ResourceType;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.InternalAction;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand;
import org.palladiosimulator.pcm.seff.seff_performance.SeffPerformanceFactory;

/**
 * @author Snowball
 */
public abstract class AbstractInternalActionDescriptor extends AbstractActionDescriptor {

    private ResourceType resourceType;

    public AbstractInternalActionDescriptor(ResourceType resourceType) {
        super();
        this.resourceType = resourceType;
    }

    public abstract String getResourceDemand();

    /**
     * @return
     */
    public ResourceType getResourceType() {
        return resourceType;
    }

    @Override
    public AbstractAction createAction() {
        ResourceType myResourceType = getResourceType();
        if (myResourceType instanceof ProcessingResourceType) {
            return createInternalAction((ProcessingResourceType) myResourceType, getResourceDemand());
        } else if (myResourceType instanceof CommunicationLinkResourceType) {
            return createInternalAction((CommunicationLinkResourceType) myResourceType, getResourceDemand());
        } else {
            throw new RuntimeException("SimuCom Completion Builder does not support resource type "
                    + myResourceType.getClass().getSimpleName());
        }
    }

    private InternalAction createInternalAction(ProcessingResourceType type, String resourceDemandSpec) {
        InternalAction action = SeffFactory.eINSTANCE.createInternalAction();
        ParametricResourceDemand d = SeffPerformanceFactory.eINSTANCE.createParametricResourceDemand();
        d.setRequiredResource_ParametricResourceDemand(type);
        PCMRandomVariable specification = CoreFactory.eINSTANCE.createPCMRandomVariable();
        specification.setSpecification(getSaveResourceDemand(resourceDemandSpec));
        d.setSpecification_ParametericResourceDemand(specification);
        action.getResourceDemand_Action().add(d);

        return action;
    }

    /**
     * We create a SEFF with a CPU demand and thus need to retrieve the CPU resource
     * 
     * @param type
     * @param resourceDemandSpec
     * @return
     */
    private InternalAction createInternalAction(CommunicationLinkResourceType type, String resourceDemandSpec) {
        InternalAction action = SeffFactory.eINSTANCE.createInternalAction();
        NetworkDemandParametricResourceDemand d = CompletionsFactory.eINSTANCE
                .createNetworkDemandParametricResourceDemand();
        d.setRequiredCommunicationLinkResource_ParametricResourceDemand(type);
        // Required to ensure valid model: requiredResource_ParametricResourceDemand has to be set
        ProcessingResourceType cpuResourceType = getCpuResourceType(type.getResourceRepository_ResourceType());
        if (cpuResourceType != null) {
            d.setRequiredResource_ParametricResourceDemand(cpuResourceType);
        } else {
            throw new RuntimeException("No CPU resource is available for SimuCom Completion Builder!");
        }

        PCMRandomVariable specification = CoreFactory.eINSTANCE.createPCMRandomVariable();
        specification.setSpecification(getSaveResourceDemand(resourceDemandSpec));
        d.setSpecification_ParametericResourceDemand(specification);
        action.getResourceDemand_Action().add(d);

        return action;
    }

    private ProcessingResourceType getCpuResourceType(ResourceRepository resourceRepository) {
        for (ResourceType availableResourceType : resourceRepository.getAvailableResourceTypes_ResourceRepository()) {
            if (availableResourceType.getEntityName().equals("CPU")) {
                return (ProcessingResourceType) availableResourceType;
            }
        }
        return null;
    }

    private String getSaveResourceDemand(String resourceDemandSpec) {
        if (resourceDemandSpec == null)
            return "0";
        if (resourceDemandSpec.equals(""))
            return "0";
        return resourceDemandSpec;
    }
}
