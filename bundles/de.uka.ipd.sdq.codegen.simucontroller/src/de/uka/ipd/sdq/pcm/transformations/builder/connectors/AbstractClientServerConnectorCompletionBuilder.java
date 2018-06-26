package de.uka.ipd.sdq.pcm.transformations.builder.connectors;

import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import de.uka.ipd.sdq.pcm.transformations.builder.IComponentBuilder;
import de.uka.ipd.sdq.pcm.transformations.builder.infrastructure.IMiddlewareInteractingComponentBuilder;
import de.uka.ipd.sdq.pcm.transformations.builder.util.PCMAndCompletionModelHolder;

public abstract class AbstractClientServerConnectorCompletionBuilder extends AbstractConnectorCompletionBuilder
        implements IClientServerConnectorCompletionComponentBuilder {

    protected OperationInterface middlewareInterface;
    private OperationRequiredRole myClientRole;
    private OperationRequiredRole myServerRole;
    private IComponentBuilder innerBuilder;

    private IMiddlewareInteractingComponentBuilder serverBuilder;
    private IMiddlewareInteractingComponentBuilder clientBuilder;

    protected ResourceContainer fromResourceContainer;
    protected ResourceContainer toResourceContainer;

    public AbstractClientServerConnectorCompletionBuilder(PCMAndCompletionModelHolder models,
            AssemblyConnector connector, ResourceContainer fromResourceContainer,
            ResourceContainer toResourceContainer, IComponentBuilder innerBuilder, String componentName) {
        super(models, connector, componentName);

        assert (models.getMiddlewareRepository().getInterfaces__Repository().get(0) instanceof OperationInterface);
        middlewareInterface = (OperationInterface) models.getMiddlewareRepository().getInterfaces__Repository().get(0);
        this.fromResourceContainer = fromResourceContainer;
        this.toResourceContainer = toResourceContainer;
        this.innerBuilder = innerBuilder;
    }

    public OperationRequiredRole getClientSideMiddlewareRole() {
        return myClientRole;
    }

    public OperationRequiredRole getServerSideMiddlewareRole() {
        return myServerRole;
    }

    protected IMiddlewareInteractingComponentBuilder getClientSideBuilder() {
        return clientBuilder;
    }

    protected IMiddlewareInteractingComponentBuilder getServerSideBuilder() {
        return serverBuilder;
    }

    protected abstract IMiddlewareInteractingComponentBuilder createClientSideBuilder();

    protected abstract IMiddlewareInteractingComponentBuilder createServerSideBuilder();

    @Override
    public void build() {
        super.build();
        this.clientBuilder = createClientSideBuilder();
        this.serverBuilder = createServerSideBuilder();

        innerBuilder.build();
        getComposedStructure().getAssemblyContexts__ComposedStructure().add(innerBuilder.getAssemblyContext());

        myClientRole = addOperationRequiredRole(middlewareInterface, "ClientMiddleware");
        myServerRole = addOperationRequiredRole(middlewareInterface, "ServerMiddleware");

        getClientSideBuilder().build();
        this.getComposedStructure().getAssemblyContexts__ComposedStructure()
                .add(getClientSideBuilder().getAssemblyContext());
        getServerSideBuilder().build();
        this.getComposedStructure().getAssemblyContexts__ComposedStructure()
                .add(getServerSideBuilder().getAssemblyContext());

        assert (myComponent.getProvidedRoles_InterfaceProvidingEntity().get(0) instanceof OperationProvidedRole);
        addProvidedDelegationConnector(getClientSideBuilder().getOperationProvidedRole(), getClientSideBuilder()
                .getAssemblyContext(), (OperationProvidedRole) myComponent.getProvidedRoles_InterfaceProvidingEntity()
                .get(0));
        assert (myComponent.getRequiredRoles_InterfaceRequiringEntity().get(0) instanceof OperationRequiredRole);
        addRequiredDelegationConnector(getServerSideBuilder().getOperationRequiredRole(), getServerSideBuilder()
                .getAssemblyContext(), (OperationRequiredRole) myComponent.getRequiredRoles_InterfaceRequiringEntity()
                .get(0));

        addAssemblyConnector(getClientSideBuilder().getOperationRequiredRole(), getClientSideBuilder()
                .getAssemblyContext(), innerBuilder.getOperationProvidedRole(), innerBuilder.getAssemblyContext());
        addAssemblyConnector(innerBuilder.getOperationRequiredRole(), innerBuilder.getAssemblyContext(),
                getServerSideBuilder().getOperationProvidedRole(), getServerSideBuilder().getAssemblyContext());

        if (innerBuilder instanceof IClientServerConnectorCompletionComponentBuilder) {
            IClientServerConnectorCompletionComponentBuilder csBuilder = (IClientServerConnectorCompletionComponentBuilder) innerBuilder;
            addRequiredDelegationConnector(csBuilder.getClientSideMiddlewareRole(), csBuilder.getAssemblyContext(),
                    getClientSideMiddlewareRole());
            addRequiredDelegationConnector(csBuilder.getServerSideMiddlewareRole(), csBuilder.getAssemblyContext(),
                    getServerSideMiddlewareRole());
        }

        addRequiredDelegationConnector(getClientSideBuilder().getMiddlewareRole(), getClientSideBuilder()
                .getAssemblyContext(), getClientSideMiddlewareRole());
        addRequiredDelegationConnector(getServerSideBuilder().getMiddlewareRole(), getServerSideBuilder()
                .getAssemblyContext(), getServerSideMiddlewareRole());
    }
}
