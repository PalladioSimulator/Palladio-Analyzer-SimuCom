package org.palladiosimulator.simframework;

import java.util.List;

import de.uka.ipd.sdq.simucomframework.resources.AbstractSimulatedResourceContainer;

public interface SimulatedResourceContainersActivation {

	/**
	 * Start all simulated resources in the simulation framework
	 */
	public void activateAllActiveResources(List<AbstractSimulatedResourceContainer> resourceContainers);

	/**
	 * Stop all resources in the simulation framework
	 */
	public void deactivateAllActiveResources(List<AbstractSimulatedResourceContainer> resourceContainers);

}