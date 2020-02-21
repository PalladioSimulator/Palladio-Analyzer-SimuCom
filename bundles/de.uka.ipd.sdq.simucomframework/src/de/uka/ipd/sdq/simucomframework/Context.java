package de.uka.ipd.sdq.simucomframework;

import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;

import de.uka.ipd.sdq.simucomframework.exceptions.ResourceContainerNotFound;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;
import de.uka.ipd.sdq.simucomframework.resources.AbstractSimulatedResourceContainer;
import de.uka.ipd.sdq.simucomframework.resources.IAssemblyAllocationLookup;
import de.uka.ipd.sdq.simucomframework.resources.ISimulatedModelEntityAccess;
import de.uka.ipd.sdq.simucomframework.resources.SimulatedLinkingResourceContainer;
import de.uka.ipd.sdq.simucomframework.variables.StackContext;

/**
 * Context of each simulation thread. This context inherits a stack context and enriches it with
 * information on the simulated execution environment.
 * 
 * @author Steffen Becker, Sebastian Lehrig
 */
public abstract class Context extends StackContext {

    /**
	 * 
	 */
    private static final long serialVersionUID = -1869414754329617190L;

    /**
     * Central registry which contains all simulated resources
     */
    private ResourceRegistry registry = null;
    
    /**
     * The thread to which this context belongs
     */
    private SimuComSimProcess myThread = null;

    /**
     * Simulation model
     */
    private SimuComModel myModel = null;

    /**
     * Initialise a new context for the given simulation model
     * 
     * @param myModel
     *            The simulation model used in this context
     */
    public Context(SimuComModel myModel) {
        if (myModel != null) { // This is for the prototype mapping, where we
            // don't need resources
            this.registry = myModel.getResourceRegistry();
            this.myModel = myModel;
        } else {
            stack.createAndPushNewStackFrame();
        }
    }

    public long getSessionId() {
        return myThread.getCurrentSessionId();
    }

    /**
     * Lookup method to find the resource container in which the given components assembly context
     * is deployed
     * 
     * @param assemblyContextID
     *            The ID of the assembly context for which its deployment is queried
     * @return The resource container in which the given assembly context is deployed
     */
    public AbstractSimulatedResourceContainer findResource(String assemblyContextID) {
        AbstractSimulatedResourceContainer container = getAssemblyAllocationLookup()
        		.getAllocatedEntity(assemblyContextID);
        if (container == null) {
            throw new ResourceContainerNotFound(
                    "Resource container for assembly context "
                            + assemblyContextID
                            + " not found. Check your allocation model. "
                            + "Note that a SubSystem must only be used once in one System, using one several times may cause this error. ");
        }
        return container;
    }

    /**
     * Lookup method to find the linking resource container that belongs to the given container id.
     * 
     * @param linkingResourceContainerID
     *            the container id
     * @return the linking resource container
     */
    public SimulatedLinkingResourceContainer findLinkingResource(String linkingResourceContainerID) {
        AbstractSimulatedResourceContainer container = registry.getResourceContainer(linkingResourceContainerID);
        if ((container == null) || !(container instanceof SimulatedLinkingResourceContainer)) {
            throw new ResourceContainerNotFound("Linking resource container for container ID "
                    + linkingResourceContainerID + " not found.");
        }
        return (SimulatedLinkingResourceContainer) container;
    }

    /**
     * The lookup allows to find the suitable simulation entity of the
     * ResourceContainer to which an AssemblyContext is deployed to.
     * 
     * Subclasses need to provide the concrete implementation of the lookup mechanism.
     * 
     * @return the AssemblyContext allocation lookup
     */
    public abstract IAssemblyAllocationLookup<AbstractSimulatedResourceContainer> getAssemblyAllocationLookup();
    
    /**
     * Provides access to simulation entities of resource containers based on their model entities.
     * 
     * @return the access facade to simulated resource containers.
     */
	public ISimulatedModelEntityAccess<ResourceContainer, AbstractSimulatedResourceContainer> getSimulatedResourceContainerAccess() {
		return this.registry::getResourceContainer;
	}
    
    public SimuComSimProcess getThread() {
        return myThread;
    }

    public void setSimProcess(SimuComSimProcess process) {
        this.myThread = process;
    }

    public SimuComModel getModel() {
        return myModel;
    }

}
