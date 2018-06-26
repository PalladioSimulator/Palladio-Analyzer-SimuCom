package de.uka.ipd.sdq.pcm.transformations;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.analyzer.workflow.blackboard.PCMResourceSetPartition;
import org.palladiosimulator.analyzer.workflow.jobs.CreatePluginProjectJob;
import org.palladiosimulator.analyzer.workflow.jobs.LoadMiddlewareConfigurationIntoBlackboardJob;
import org.palladiosimulator.analyzer.workflow.jobs.LoadPCMModelsIntoBlackboardJob;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationFactory;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.CompositionFactory;
import org.palladiosimulator.pcm.core.composition.CompositionPackage;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;

import de.uka.ipd.sdq.codegen.simucontroller.runconfig.AbstractSimulationWorkflowConfiguration;
import de.uka.ipd.sdq.featureconfig.Configuration;
import de.uka.ipd.sdq.pcm.transformations.builder.connectors.ConnectorReplacingBuilder;
import de.uka.ipd.sdq.pcm.transformations.builder.util.PCMAndCompletionModelHolder;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.MDSDBlackboard;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.ResourceSetPartition;

public class ApplyConnectorCompletionsJob implements IBlackboardInteractingJob<MDSDBlackboard> {

    private static final Logger LOGGER = Logger.getLogger(ApplyConnectorCompletionsJob.class);

    public static final String COMPLETION_REPOSITORY_PARTITION = "de.uka.ipd.sdq.pcm.completionRepositoryPartition";

	private static final String COMPLETIONS_FOLDER = "model/connector-completion";

    private MDSDBlackboard blackboard;
    private final AbstractSimulationWorkflowConfiguration configuration;

    public ApplyConnectorCompletionsJob(AbstractSimulationWorkflowConfiguration configuration) {
        super();

        this.configuration = configuration;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {

        PCMResourceSetPartition pcmModels = (PCMResourceSetPartition) blackboard
                .getPartition(LoadPCMModelsIntoBlackboardJob.PCM_MODELS_PARTITION_ID);
        ResourceSetPartition middlewareRepository = blackboard
                .getPartition(LoadMiddlewareConfigurationIntoBlackboardJob.RMI_MIDDLEWARE_PARTITION_ID);

        if (LOGGER.isEnabledFor(Level.INFO)) {
            LOGGER.info("Create completion repository...");
        }
        ResourceSetPartition completionRepositoryPartition = new ResourceSetPartition();
        Repository completionRepository = RepositoryFactory.eINSTANCE.createRepository();
        String completionRepositoryName = "CompletionsRepository";
        completionRepository.setEntityName(completionRepositoryName);
        
        IFolder completionFolder = getOrCreateCompletionFolder();
        
        final URI completionFolderURI = URI.createFileURI(completionFolder.getLocation().toOSString());
        
        URI completionRepositoryURI = completionFolderURI.appendSegment("completions.repository");
        Resource r = completionRepositoryPartition.getResourceSet().createResource(completionRepositoryURI);
        r.getContents().add(completionRepository);
        this.blackboard.addPartition(COMPLETION_REPOSITORY_PARTITION, completionRepositoryPartition);

        final PCMAndCompletionModelHolder models = new PCMAndCompletionModelHolder(
                pcmModels.getResourceTypeRepository(), null, pcmModels.getSystem(), pcmModels.getAllocation(), null,
                completionRepository, (Repository) middlewareRepository.getResourceSet().getResources().get(0)
                        .getContents().get(0));

        final Configuration featureConfiguration = pcmModels.getFeatureConfig();

        new AllInstancesTransformer<ResourceContainer>(ResourceenvironmentPackage.eINSTANCE.getResourceContainer(),
                models.getAllocation().getTargetResourceEnvironment_Allocation()) {

            @Override
            protected void transform(ResourceContainer object) {
                addMiddleware(models, object);
            }

        }.transform();

        if (LOGGER.isEnabledFor(Level.INFO)) {
            LOGGER.info("Replace connectors with completions...");
        }
        new AllInstancesTransformer<AssemblyConnector>(CompositionPackage.eINSTANCE.getAssemblyConnector(),
                models.getSystem()) {

            @Override
            protected void transform(AssemblyConnector connector) {
                if (configuration.getSimulateLinkingResources()) {
                    ConnectorReplacingBuilder replacer = new ConnectorReplacingBuilder(models, connector,
                            featureConfiguration.getDefaultConfig());
                    replacer.build();
                }
            }

        }.transform();
        
        try{
        	completionRepositoryPartition.storeAllResources();
        } catch (final IOException e) {
            if(LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error("Unable to serialize the working copy of the completion models." ,e);
            }
        }
    }
    
    private IFolder getOrCreateCompletionFolder() throws JobFailedException{
        assert (this.configuration != null);
        final IProject project = CreatePluginProjectJob.getProject(this.configuration.getStoragePluginID());
        assert (project != null);
        
        return CreatePluginProjectJob.getOrCreateFolder(project, COMPLETIONS_FOLDER);
    }

    /**
     * Creates a middleware component instance and allocates it to the given resource container
     */
    private void addMiddleware(PCMAndCompletionModelHolder models, ResourceContainer resContainer) {
        AssemblyContext ctx = CompositionFactory.eINSTANCE.createAssemblyContext();
        ctx.setEntityName("AssCtx Middleware " + resContainer.getEntityName());
        ctx.setEncapsulatedComponent__AssemblyContext(models.getMiddlewareRepository().getComponents__Repository()
                .get(0)); // TODO: Parameterise me!
        models.getSystem().getAssemblyContexts__ComposedStructure().add(ctx);

        models.getSystem().getAssemblyContexts__ComposedStructure().add(ctx);
        AllocationContext allocCtx = AllocationFactory.eINSTANCE.createAllocationContext();
        allocCtx.setEntityName("AllocCtx Middleware " + resContainer.getEntityName());
        allocCtx.setAssemblyContext_AllocationContext(ctx);
        allocCtx.setResourceContainer_AllocationContext(resContainer);
        models.getAllocation().getAllocationContexts_Allocation().add(allocCtx);

        if (LOGGER.isEnabledFor(Level.INFO)) {
            LOGGER.info("Added middleware component >"
                    + ctx.getEncapsulatedComponent__AssemblyContext().getEntityName() + "< to resource container >"
                    + resContainer.getEntityName() + "<");
        }
    }

    @Override
    public void setBlackboard(MDSDBlackboard blackboard) {
        this.blackboard = blackboard;
    }

    @Override
    public String getName() {
        return "Add connector completions job";
    }

    @Override
    public void cleanup(IProgressMonitor monitor) throws CleanupFailedException {
        // Nothing to do here
    }
}
