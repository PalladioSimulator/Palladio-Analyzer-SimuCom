package de.uka.ipd.sdq.simucomframework.resources;

import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;

import de.uka.ipd.sdq.simucomframework.model.SimuComModel;

/**
 * This interface has to be implemented by classes that use the Scheduled Resource extension point.
 * This interface should be implemented by a factory class that creates a new ScheduledResource. The
 * new scheduled resource can also be a custom-defined subclass of ScheduledResource.java and
 * overwrite methods from ScheduledResource.java.
 * 
 */
public interface IScheduledResourceFactory {

    /**
     * @param activeResource
     *            the ProcessingResourceSpecification e.g. CPU,HDD,DELAY, or a custom specified
     *            ProcessingResourceSpecification
     * @param model
     *            the SimuComModel
     * @param resourceContainerID
     *            the resourceContainerId of the container containing this ScheduledResource
     * @param schedulingStrategyID
     *            the schedulingStrategyID of this ScheduledResource
     * @return a ScheduledResource
     */
    public ScheduledResource createScheduledResource(ProcessingResourceSpecification activeResource, SimuComModel model,
            String resourceContainerID, String schedulingStrategyID);
}