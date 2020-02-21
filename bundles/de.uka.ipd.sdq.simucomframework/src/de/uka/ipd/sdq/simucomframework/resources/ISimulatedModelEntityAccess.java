package de.uka.ipd.sdq.simucomframework.resources;

import de.uka.ipd.sdq.identifier.Identifier;

/**
 * Through the ISimulatedModelEntityAccess interface it is possible to access
 * the current simulation entity responsible for a particular entity of the
 * model.
 * 
 * Furthermore, this interfaces allows for multiple contexts to share the same
 * lookup information.
 * 
 * @author Sebastian Krach
 *
 */
@FunctionalInterface
public interface ISimulatedModelEntityAccess<ModelEntity extends Identifier, SimulatedModelEntityType> {

    /**
     * Gets the simulation entitiy for the model entity identified by the provided id.
     */
    SimulatedModelEntityType getSimulatedEntity(String modelEntityIdentifier);

    /**
     * Gets the simulation entitiy for the model entity.
     */
    default SimulatedModelEntityType getSimulatedEntity(ModelEntity modelEntity) {
        return getSimulatedEntity(modelEntity.getId());
    }

}
