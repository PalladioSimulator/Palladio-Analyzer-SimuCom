package de.uka.ipd.sdq.pcm.transformations.builder;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.RepositoryComponent;

public interface IComponentBuilder extends IBuilder {

    AssemblyContext getAssemblyContext();

    OperationProvidedRole getOperationProvidedRole();

    OperationRequiredRole getOperationRequiredRole();

    RepositoryComponent getComponent();
}
