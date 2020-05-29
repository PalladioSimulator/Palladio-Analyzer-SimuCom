package org.palladiosimulator.simframework;

import de.uka.ipd.sdq.simucomframework.model.SimuComModel;
import de.uka.ipd.sdq.simucomframework.resources.AbstractSimulatedResourceContainer;
import de.uka.ipd.sdq.simucomframework.resources.SimulatedLinkingResourceContainer;
import de.uka.ipd.sdq.simucomframework.resources.SimulatedResourceContainer;

public abstract class AbstractSimulatedResourceContainerFactory {

	/**
	 * Create a PCM ResourceContainer
	 *
	 * @param containerID
	 *            PCM ID of the resource container to create
	 * @return The simulated resource container object
	 */
	public static AbstractSimulatedResourceContainer createResourceContainer(final SimuComModel myModel, String containerID) {
        return new SimulatedResourceContainer(myModel, containerID);
    }

	/**
	 * Create a simulated PCM LinkingResource
	 *
	 * @param containerID
	 *            PCM ID of the LinkingResource
	 * @return The resource container introduced for the linking resource. Note, this container is
	 *         virtual as it does not exist in the PCMs original model. However, it exists in the
	 *         simulation to unify resource container and link resource behavior.
	 */
	public static AbstractSimulatedResourceContainer createLinkingResourceContainer(final SimuComModel myModel, String containerID) {
        return new SimulatedLinkingResourceContainer(myModel, containerID);
    }

}