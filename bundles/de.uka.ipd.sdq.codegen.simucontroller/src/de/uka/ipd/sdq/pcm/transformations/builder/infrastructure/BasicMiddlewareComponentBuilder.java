package de.uka.ipd.sdq.pcm.transformations.builder.infrastructure;

import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import de.uka.ipd.sdq.pcm.transformations.builder.abstractbuilder.BasicComponentBuilder;
import de.uka.ipd.sdq.pcm.transformations.builder.seff.DelegatorComponentSeffBuilder;
import de.uka.ipd.sdq.pcm.transformations.builder.seff.MiddlewareComponentSeffBuilder;
import de.uka.ipd.sdq.pcm.transformations.builder.util.PCMAndCompletionModelHolder;

public class BasicMiddlewareComponentBuilder extends BasicComponentBuilder implements
        IMiddlewareInteractingComponentBuilder {

    protected OperationInterface middleware;
    private OperationRequiredRole myRequiredMiddlewareInterface;

    public BasicMiddlewareComponentBuilder(PCMAndCompletionModelHolder models, OperationInterface providedIf,
            OperationInterface requiredIf, OperationInterface middlewareInterface, ResourceContainer container,
            String componentName) {
        super(models, providedIf, requiredIf, container, componentName);

        middleware = middlewareInterface;
    }

    @Override
    public void build() {
        myRequiredMiddlewareInterface = addOperationRequiredRole(middleware, "Middleware");
        super.build();
    }

    @Override
    protected DelegatorComponentSeffBuilder getSeffBuilder() {
        return new MiddlewareComponentSeffBuilder(getOperationProvidedRole(), getOperationRequiredRole(),
                getMiddlewareRole());
    }

    public OperationRequiredRole getMiddlewareRole() {
        return myRequiredMiddlewareInterface;
    }

}
