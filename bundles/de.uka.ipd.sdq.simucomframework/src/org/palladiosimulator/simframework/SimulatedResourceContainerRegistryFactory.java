package org.palladiosimulator.simframework;

import de.uka.ipd.sdq.simucomframework.ResourceRegistry;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;
/**
 * Factory for SimulatedResourceContainerRegistry
 * @author Jens
 *
 */
public abstract class SimulatedResourceContainerRegistryfactory {
	
	public static SimulatedResourceContainerRegistry createSimulatedResourceContainerRegistry(final SimuComModel model) {
        return new ResourceRegistry(model);
    }

	
}
