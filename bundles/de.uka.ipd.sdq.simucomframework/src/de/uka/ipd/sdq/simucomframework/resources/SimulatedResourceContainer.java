package de.uka.ipd.sdq.simucomframework.resources;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.resourceenvironment.HDDProcessingResourceSpecification;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;

import de.uka.ipd.sdq.scheduler.IPassiveResource;
import de.uka.ipd.sdq.simucomframework.SimuComSimProcess;
import de.uka.ipd.sdq.simucomframework.exceptions.ResourceContainerIsMissingRequiredResourceType;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;

/**
 * TODO Get rid of heavy argument passing. Model elements are often enough as they contain
 * information like ID and name. [Lehrig]
 *
 * TODO Find original author ;)
 *
 * @author Steffen Becker, ???, Sebastian Lehrig
 */
public class SimulatedResourceContainer extends AbstractSimulatedResourceContainer {

    private final List<SimulatedResourceContainer> nestedResourceContainers;
    private SimulatedResourceContainer parentResourceContainer;

    public SimulatedResourceContainer(final SimuComModel myModel, final String containerID) {
        this(myModel, containerID, new LinkedList<SimulatedResourceContainer>(), null);
    }

    protected SimulatedResourceContainer(final SimuComModel myModel, final String containerID,
            final List<SimulatedResourceContainer> nestedContainer, final SimulatedResourceContainer parent) {
        super(myModel, containerID);

        this.nestedResourceContainers = nestedContainer;
        this.parentResourceContainer = parent;
    }

    public IPassiveResource createPassiveResource(final PassiveResource resource, final AssemblyContext assemblyContext,
            final long capacity) {
        final IPassiveResource newPassiveResource = getSimplePassiveResource(resource, assemblyContext, this.myModel,
                capacity);

        // setup calculators
        CalculatorHelper.setupPassiveResourceStateCalculator(newPassiveResource, this.myModel);
        CalculatorHelper.setupWaitingTimeCalculator(newPassiveResource, this.myModel);
        CalculatorHelper.setupHoldTimeCalculator(newPassiveResource, this.myModel);

        return newPassiveResource;
    }

    public List<SimulatedResourceContainer> getNestedResourceContainers() {
        return this.nestedResourceContainers;
    }

    public SimulatedResourceContainer getParentResourceContainer() {
        return this.parentResourceContainer;
    }

    public void addNestedResourceContainer(final String nestedResourceContainerId) {
        final AbstractSimulatedResourceContainer resourceContainer = this.myModel.getResourceRegistry()
            .getResourceContainer(nestedResourceContainerId);
        if ((resourceContainer == null) || (!(resourceContainer instanceof SimulatedResourceContainer))) {
            throw new RuntimeException("Could not initialize resouce container " + this.myContainerID
                    + ": Nested resource container " + nestedResourceContainerId + " is not available.");
        }
        this.nestedResourceContainers.add((SimulatedResourceContainer) resourceContainer);
    }

    public void setParentResourceContainer(final String parentResourceContainerId) {
        final AbstractSimulatedResourceContainer resourceContainer = this.myModel.getResourceRegistry()
            .getResourceContainer(parentResourceContainerId);
        if ((resourceContainer == null) || (!(resourceContainer instanceof SimulatedResourceContainer))) {
            throw new RuntimeException("Could not initialize resouce container " + this.myContainerID
                    + ": Parent resource container " + parentResourceContainerId + " is not available.");
        }
        this.parentResourceContainer = (SimulatedResourceContainer) resourceContainer;
    }

    public void addActiveResource(final ProcessingResourceSpecification activeResource,
            final String[] providedInterfaceIds, final String resourceContainerID, final String schedulingStrategyID) {
        final ScheduledResource r = addActiveResourceWithoutCalculators(activeResource, providedInterfaceIds,
                resourceContainerID, schedulingStrategyID);

        // setup calculators
        // TODO: setup waiting time calculator
        // CalculatorHelper.setupWaitingTimeCalculator(r, this.myModel);
        CalculatorHelper.setupDemandCalculator(r, this.myModel);

        // setup utilization calculators depending on their scheduling strategy
        // and number of cores
        if (schedulingStrategyID.equals(SchedulingStrategy.PROCESSOR_SHARING)) {
            if (r.getNumberOfInstances() == 1) {
                CalculatorHelper.setupActiveResourceStateCalculators(r, this.myModel);
            } else {
                CalculatorHelper.setupOverallUtilizationCalculator(r, this.myModel);
            }
        } else if (schedulingStrategyID.equals(SchedulingStrategy.DELAY)
                || schedulingStrategyID.equals(SchedulingStrategy.FCFS)) {
            assert (r.getNumberOfInstances() == 1) : "DELAY and FCFS resources are expected to "
                    + "have exactly one core";
            CalculatorHelper.setupActiveResourceStateCalculators(r, this.myModel);
        } else {
            // Use an OverallUtilizationCalculator by default.
            CalculatorHelper.setupOverallUtilizationCalculator(r, this.myModel);
        }
    }

    private static final String ScheduledResourceExtensionPointId = "de.uka.ipd.sdq.simucomframework.resources.scheduledresource";
    private static final String ScheduledResourceExtensionPointAttribute_Class = "class";

    private IExtension[] getRegisteredSchedulerExtensions() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(ScheduledResourceExtensionPointId);
        if (extensionPoint == null) {
            // No extension point found!
            return null;
        }
        IExtension[] extensions = extensionPoint.getExtensions();
        return extensions;
    }

    private ScheduledResource createScheduledResourceFromExtension(ProcessingResourceSpecification activeResource,
            SimuComModel myModel, String resourceContainerID, String schedulingStrategyID) {
        IExtension[] registeredExtensions = getRegisteredSchedulerExtensions();
        if (registeredExtensions == null) {
            if (LOGGER.isEnabledFor(Level.DEBUG)) {
                LOGGER.debug("No ProcessingResourceType extensions available!");
            }
            return null;
        }

        for (int i = 0; i < registeredExtensions.length; i++) {
            IExtension registeredExtension = registeredExtensions[i];
            IConfigurationElement[] elements = registeredExtension.getConfigurationElements();
            String extensionIdentifer = registeredExtension.getUniqueIdentifier();
            if (extensionIdentifer.equals(activeResource.getActiveResourceType_ActiveResourceSpecification()
                .getId())) {
                for (int j = 0; j < elements.length; j++) {
                    IConfigurationElement element = elements[j];
                    Object executableExtension = null;
                    try {
                        executableExtension = element
                            .createExecutableExtension(ScheduledResourceExtensionPointAttribute_Class);
                    } catch (CoreException e) {
                    }
                    if ((executableExtension != null) && (executableExtension instanceof IScheduledResourceFactory)) {
                        return ((IScheduledResourceFactory) executableExtension).createScheduledResource(activeResource,
                                this.myModel, resourceContainerID, schedulingStrategyID);
                    }
                }
            }
        }
        if (LOGGER.isEnabledFor(Level.DEBUG)) {
            LOGGER.debug("No ProcessingResourceType extension for ID: s "
                    + activeResource.getActiveResourceType_ActiveResourceSpecification()
                        .getId()
                    + " found!");
        }
        return null;
    }

    public ScheduledResource addActiveResourceWithoutCalculators(final ProcessingResourceSpecification activeResource,
            final String[] providedInterfaceIds, final String resourceContainerID, final String schedulingStrategyID) {

        ScheduledResource scheduledResource = null;
        ScheduledResource createdScheduledResourceFromExtension = createScheduledResourceFromExtension(activeResource,
                this.myModel, resourceContainerID, schedulingStrategyID);

        if (createdScheduledResourceFromExtension != null) {
            scheduledResource = createdScheduledResourceFromExtension;
        } else if (activeResource instanceof HDDProcessingResourceSpecification) {
            scheduledResource = new HDDResource((HDDProcessingResourceSpecification) activeResource, this.myModel,
                    resourceContainerID, schedulingStrategyID);
        } else {
            scheduledResource = new ScheduledResource(activeResource, this.myModel, resourceContainerID,
                    schedulingStrategyID);
        }

        final String resourceType = activeResource.getActiveResourceType_ActiveResourceSpecification()
            .getId();

        this.activeResources.put(resourceType, scheduledResource);
        // Currently, resources can also be looked up by the provided interface id
        if (providedInterfaceIds != null) {
            for (final String providedInterfaceId : providedInterfaceIds) {
                this.activeResourceProvidedInterfaces.put(providedInterfaceId, resourceType);
            }
        }

        return scheduledResource;
    }

    private IPassiveResource getSimplePassiveResource(final PassiveResource resource,
            final AssemblyContext assemblyContext, final SimuComModel simuComModel, final long capacity) {
        return new SimSimpleFairPassiveResource(resource, assemblyContext, simuComModel, capacity);
    }
    
    /**
     * Demand processing of a resource demand by a given type of active
     * resources. In future versions this has to control schedulers of resource
     * types which exist in multiple instances, use parent container when resource not found.
     *
     * @param requestingProcess
     *            The thread requesting the processing of a resource demand
     * @param resourceServiceID
     *            the id of the resource service to be called.
     * @param typeID
     *            ID of the resource type to which the demand is directed. Same
     *            as the PCM resource type IDs
     * @param demand
     *            The demand in units processable by the resource. The resource
     *            is responsible itself for converting this demand into time
     *            spans
     */
    @Override
    public void loadActiveResource(final SimuComSimProcess requestingProcess, final int resourceServiceID,
            final String typeID, final double demand) {
    	  try {
              super.loadActiveResource(requestingProcess, resourceServiceID, typeID, demand);
          } catch (final ResourceContainerIsMissingRequiredResourceType e) {
              if (this.parentResourceContainer == null) {
                  if (LOGGER.isEnabledFor(Level.ERROR)) {
                      LOGGER.error("Resource container is missing a resource which was attempted to be loaded"
                              + " by a component and has no parent Resource Container to look in. ID of resource type was: "
                              + typeID);
                  }
                  throw e;
              } else {
                  this.parentResourceContainer.loadActiveResource(requestingProcess, resourceServiceID, typeID, demand);
              }
          }
    }

    /**
     * Demand processing of a resource demand by a given type of active resources. If the resource
     * container has no own resources, look in parent resource container.
     *
     * @param requestingProcess
     *            The thread requesting the processing of a resouce demand
     * @param typeID
     *            ID of the resource type to which the demand is directed. Same as the PCM resource
     *            type IDs
     * @param demand
     *            The demand in units processable by the resource. The resource is responsible
     *            itself for converting this demand into time spans
     */
    @Override
    public void loadActiveResource(final SimuComSimProcess requestingProcess, final String typeID,
            final double demand) {
        try {
            super.loadActiveResource(requestingProcess, typeID, demand);
        } catch (final ResourceContainerIsMissingRequiredResourceType e) {
            if (this.parentResourceContainer == null) {
                if (LOGGER.isEnabledFor(Level.ERROR)) {
                    LOGGER.error("Resource container is missing a resource which was attempted to be loaded"
                            + " by a component and has no parent Resource Container to look in. ID of resource type was: "
                            + typeID);
                }
                throw e;
            } else {
                this.parentResourceContainer.loadActiveResource(requestingProcess, typeID, demand);
            }
        }
    }

    /**
     * Demand processing of a resource demand by a given type of active resource and a resource
     * interface operation. If the resource container has no own resources, look in parent resource
     * container.
     *
     * @param requestingProcess
     *            The thread requesting the processing of a resource demand
     * @param typeID
     *            ID of the resource provided interface to which the demand is directed.
     * @param resourceServiceID
     *            the id of the resource service to be called.
     * @param demand
     *            The demand in units processable by the resource. The resource is responsible
     *            itself for converting this demand into time spans
     */
    @Override
    public void loadActiveResource(final SimuComSimProcess requestingProcess, final String providedInterfaceID,
            final int resourceServiceID, final double demand) {
        try {
            super.loadActiveResource(requestingProcess, providedInterfaceID, resourceServiceID, demand);
        } catch (final ResourceContainerIsMissingRequiredResourceType e) {
            if (this.parentResourceContainer == null) {
                if (LOGGER.isEnabledFor(Level.ERROR)) {
                    LOGGER.error("Resource container is missing a resource which was attempted to be loaded"
                            + " by a component and has no parent Resource Container to look in. ID of resource type was: "
                            + e.getTypeID());
                }
                throw e;
            } else {
                this.parentResourceContainer.loadActiveResource(requestingProcess, providedInterfaceID,
                        resourceServiceID, demand);
            }
        }
    }

    /**
     * Demand processing of a resource demand by a given type of active resource and a resource
     * interface operation and additional parameters which can be used in an active resource.
     *
     * @param requestingProcess
     *            The thread requesting the processing of a resource demand
     * @param providedInterfaceID
     *            ID of the resource provided interface to which the demand is directed.
     * @param resourceServiceID
     *            the id of the resource service to be called. itself for converting this demand
     *            into time spans
     * @param parameterMap
     *            Additional Parameters usable in an active resource. Parameters represented as
     *            <parameterName, specification>
     * @param demand
     *            The demand in units processable by the resource. The resource is responsible
     *
     */
    @Override
    public void loadActiveResource(final SimuComSimProcess requestingProcess, final String providedInterfaceID,
            final int resourceServiceID, final Map<String, Serializable> parameterMap, final double demand) {
        try {
            super.loadActiveResource(requestingProcess, providedInterfaceID, resourceServiceID, parameterMap, demand);
        } catch (final ResourceContainerIsMissingRequiredResourceType e) {
            if (this.parentResourceContainer == null) {
                if (LOGGER.isEnabledFor(Level.ERROR)) {
                    LOGGER.error("Resource container is missing a resource which was attempted to be loaded"
                            + " by a component and has no parent Resource Container to look in. ID of resource type was: "
                            + e.getTypeID());
                }
                throw e;
            } else {
                this.parentResourceContainer.loadActiveResource(requestingProcess, providedInterfaceID,
                        resourceServiceID, parameterMap, demand);
            }
        }
    }
}
