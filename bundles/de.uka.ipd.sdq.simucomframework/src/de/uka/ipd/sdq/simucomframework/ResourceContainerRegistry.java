package de.uka.ipd.sdq.simucomframework;

import de.uka.ipd.sdq.simucomframework.resources.AbstractSimulatedResourceContainer;
/**
 * Interface for central registery for simulated resources
 * @author Jens Manig
 *
 */

public interface ResourceContainerRegistry {

	/**
	 * Add a PCM ResourceContainer
	 *
	 * @param container
	 *            the resource container to add
	 */
	//void addResourceContainer(AbstractSimulatedResourceContainer container);

	/**
     * @param resourceContainerID
     *            ID of the container
     * @return True if the given ID is known in the resource registry
     */
    //public boolean containsResourceContainer(final String resourceContainerID);
	
	/**
	 * Retrieve the resource container with the given ID
	 *
	 * @param resourceContainerID
	 *            ID of the container to retrieve. The container must exist in this registry
	 * @return The queried resource container
	 */
	AbstractSimulatedResourceContainer getResourceContainer(String resourceContainerID);

	/**
	 * Remove the resource container with the given ID
	 *
	 * @param resourceContainerID
	 *            ID of the container to remove. The container must exist in this registry
	 * @return The queried resource container
	 */
	//AbstractSimulatedResourceContainer removeResourceContainerFromRegistry(String resourceContainerID);

}