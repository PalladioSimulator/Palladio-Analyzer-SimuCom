package de.uka.ipd.sdq.simucomframework;

import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.commons.emfutils.EMFLoadHelper;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

import de.uka.ipd.sdq.scheduler.IPassiveResource;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;
import de.uka.ipd.sdq.simucomframework.resources.AbstractSimulatedResourceContainer;
import de.uka.ipd.sdq.simucomframework.resources.HashMapAssemblyAllocationLookup;
import de.uka.ipd.sdq.simucomframework.resources.IAssemblyAllocationLookup;
import de.uka.ipd.sdq.simucomframework.resources.SimulatedResourceContainer;

/**
 * 
 * Context of each thread in SimuCom simulation.
 * 
 * This class contains the functionality previously contained in
 * <code>Context</code> which only works for SimuCom simulations.
 * 
 * @author Sebastian Krach
 *
 */
public abstract class SimuComContext extends Context {
    /**
     * 
     */
    private static final long serialVersionUID = 8628196416449895566L;

    /**
     * AssemblyContextID -> PassiveRessource
     */
    private final HashMap<String, IPassiveResource> assemblyPassiveResourceHash = new HashMap<String, IPassiveResource>();

    /**
     * This Hashmap constitutes the backing storage for the static deployment lookup
     * table of SimuCom simulations
     * 
     * AssemblyContextID -> Abstract SimulatedResourceContainer
     */
    private Map<String, AbstractSimulatedResourceContainer> assemblyLinkMap = new HashMap<>();

    private IAssemblyAllocationLookup<AbstractSimulatedResourceContainer> assemblyLinkLookup = 
            new HashMapAssemblyAllocationLookup<AbstractSimulatedResourceContainer>(assemblyLinkMap);

    public SimuComContext(SimuComModel myModel) {
        super(myModel);
        initialiseAssemblyContextLookup();
    }

    /**
     * Create a deployment relationship between the given assembly context and the
     * given resource container
     * 
     * @param assemblyContextID   ID of the assembly context to allocate
     * @param resourceContainerID ID of the resource container on which the assembly
     *                            context is allocated
     */
    protected void linkAssemblyContextAndResourceContainer(String assemblyContextID, String resourceContainerID) {
        assert getSimulatedResourceContainerAccess().getSimulatedEntity(resourceContainerID) != null;
        AbstractSimulatedResourceContainer container = getSimulatedResourceContainerAccess()
                .getSimulatedEntity(resourceContainerID);
        assemblyLinkMap.put(assemblyContextID, container);
    }

    /**
     * This method is used by SimuCom simulation code to look up simulated instances
     * of passive resources based on the assembly context
     */
    public IPassiveResource getPassiveRessourceInContext(final String resourceURI,
            final AssemblyContext assemblyContext, AbstractSimulatedResourceContainer resourceContainer,
            long capacity) {
        final PassiveResource resource = (PassiveResource) EMFLoadHelper.loadAndResolveEObject(resourceURI);
        IPassiveResource pr = assemblyPassiveResourceHash.get(assemblyContext.getId() + resource.getId());

        if (pr == null) {
            pr = ((SimulatedResourceContainer) resourceContainer).createPassiveResource(resource, assemblyContext,
                    capacity);
            assemblyPassiveResourceHash.put(assemblyContext.getId() + resource.getId(), pr);
        }

        return pr;
    }

    /**
     * Template method to be filled in by the generator. Calles
     * linkAssemblyContextAndResourceContainer to create the deployment specified in
     * the allocation model
     */
    protected abstract void initialiseAssemblyContextLookup();

    @Override
    public IAssemblyAllocationLookup<AbstractSimulatedResourceContainer> getAssemblyAllocationLookup() {
        return assemblyLinkLookup;
    }
}
