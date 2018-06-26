package de.uka.ipd.sdq.pcm.transformations.builder.connectors;

import org.palladiosimulator.analyzer.completions.Completion;
import org.palladiosimulator.analyzer.completions.CompletionsFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import de.uka.ipd.sdq.pcm.transformations.builder.abstractbuilder.AbstractCompositeStructureBuilder;
import de.uka.ipd.sdq.pcm.transformations.builder.util.PCMAndCompletionModelHolder;

public abstract class AbstractConnectorCompletionBuilder extends AbstractCompositeStructureBuilder {

    protected AssemblyConnector connectorToReplace;
    private static long contextID = 0;

    public AbstractConnectorCompletionBuilder(PCMAndCompletionModelHolder models, AssemblyConnector con,
            String componentName) {
        super(models, componentName);
        this.connectorToReplace = con;
    }

    @Override
    protected RepositoryComponent createComponent(String componentName) {
        Completion completion = CompletionsFactory.eINSTANCE.createCompletion();
        completion.setEntityName("ConnectorCompletion_" + componentName + getNextCounter());

        return completion;
    }

    @Override
    public void build() {
        super.build();
        myOperationProvidedRole = addOperationProvidedRole(connectorToReplace.getRequiredRole_AssemblyConnector()
                .getRequiredInterface__OperationRequiredRole(), "ProvidedRole");
        myOperationRequiredRole = addOperationRequiredRole(connectorToReplace.getRequiredRole_AssemblyConnector()
                .getRequiredInterface__OperationRequiredRole(), "RequiredRole");

        myAssemblyContext = CompositionFactory.eINSTANCE.createAssemblyContext();
        myAssemblyContext.setEntityName("CompletionComponentContext" + contextID);
        contextID++;
        myAssemblyContext.setEncapsulatedComponent__AssemblyContext(myComponent);
    }

}
