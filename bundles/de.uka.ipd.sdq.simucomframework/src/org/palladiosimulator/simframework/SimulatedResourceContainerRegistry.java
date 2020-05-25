package org.palladiosimulator.simframework;

import java.util.List;

import de.uka.ipd.sdq.simucomframework.resources.AbstractSimulatedResourceContainer;
import de.uka.ipd.sdq.simucomframework.resources.SimulatedResourceContainer;
/**
 * Interface for central registery for simulated resources
 * @author Jens Manig
 *
 */

public interface SimulatedResourceContainerRegistry {

	/**
	 * Add a PCM ResourceContainer
	 *
	 * @param container
	 *            the resource container to add
	 */
	void addResourceContainer(AbstractSimulatedResourceContainer container);

	/**
     * @param resourceContainerID
     *            ID of the container
     * @return True if the given ID is known in the resource registry
     */
    public boolean containsResourceContainer(final String resourceContainerID);
	
	/**
	 * Retrieve the resource container with the given ID
	 *
	 * @param resourceContainerID
	 *            ID of the container to retrieve. The container must exist in this registry
	 * @return The queried resource container
	 */
	AbstractSimulatedResourceContainer getResourceContainer(String resourceContainerID);
	
	 public List<SimulatedResourceContainer> getSimulatedResourceContainers();

}