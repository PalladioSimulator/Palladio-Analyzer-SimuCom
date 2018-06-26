package de.uka.ipd.sdq.pcm.transformations.builder.infrastructure;

import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import de.uka.ipd.sdq.pcm.transformations.builder.IComponentBuilder;

public interface IMiddlewareInteractingComponentBuilder extends IComponentBuilder {

    /**
		 */
    public abstract OperationRequiredRole getMiddlewareRole();

}
