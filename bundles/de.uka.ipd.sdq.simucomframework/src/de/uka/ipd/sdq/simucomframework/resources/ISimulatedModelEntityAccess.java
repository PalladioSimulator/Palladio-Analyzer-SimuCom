package de.uka.ipd.sdq.simucomframework.resources;

import de.uka.ipd.sdq.identifier.Identifier;

/**
 * Through the ISimulatedModelEntityAccess interface it is possible to access the
 * current simulation entity responsible for a particular entity of the model.
 * 
 * Furthermore, this interfaces allows for multiple contexts to share the same
 * lookup information.
 * 
 * @author Sebastian Krach
 *
 */
@FunctionalInterface
public interface ISimulatedModelEntityAccess<ModelEntity extends Identifier, SimulatedModelEntityType> {
	
	SimulatedModelEntityType getSimulatedEntity(String modelEntityIdentifier);
	
	default SimulatedModelEntityType getSimulatedEntity(ModelEntity modelEntity) {
		return getSimulatedEntity(modelEntity.getId());
	}

}
