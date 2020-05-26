package org.palladiosimulator.simframework;

import de.uka.ipd.sdq.simucomframework.model.SimuComModel;

public interface SimulatedResourceContainerRegistryfactory {
	
	public SimulatedResourceContainerRegistry createSimulatedResourceContainerRegistry(final SimuComModel model);
	
}
