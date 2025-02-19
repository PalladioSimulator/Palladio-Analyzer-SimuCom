package de.uka.ipd.sdq.pcm.transformations;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.analyzer.workflow.core.blackboard.PCMResourceSetPartition;
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
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;

import com.google.common.collect.Streams;

import de.uka.ipd.sdq.codegen.simucontroller.runconfig.AbstractSimulationWorkflowConfiguration;
import de.uka.ipd.sdq.featureconfig.Configuration;
import de.uka.ipd.sdq.pcm.transformations.builder.IBuilder;
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

    protected MDSDBlackboard blackboard;
    private final AbstractSimulationWorkflowConfiguration configuration;

    public ApplyConnectorCompletionsJob(AbstractSimulationWorkflowConfiguration configuration) {
        super();

        this.configuration = configuration;
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        PCMResourceSetPartition pcmModels = (PCMResourceSetPartition) blackboard
                .getPartition(LoadPCMModelsIntoBlackboardJob.PCM_MODELS_PARTITION_ID);
        
        if (LOGGER.isEnabledFor(Level.INFO)) {
            LOGGER.info("Create completion repository...");
        }
        
        var middlewareRepo = getMiddlewareRepository();
        
        var completionsRepositoryPartition = getPartitionForCompletedMiddleware();
        var completionRepository = createCompletionRepository(completionsRepositoryPartition, middlewareRepo);

        final PCMAndCompletionModelHolder models = new PCMAndCompletionModelHolder(
                pcmModels.getResourceTypeRepository(), null, pcmModels.getSystem(), pcmModels.getAllocation(), null,
                completionRepository, middlewareRepo);

        Streams.stream(models.getAllocation()
            .getTargetResourceEnvironment_Allocation()
            .eAllContents())
            .filter(ResourceenvironmentPackage.Literals.RESOURCE_CONTAINER::isInstance)
            .map(ResourceContainer.class::cast)
            .forEach(container -> addOneMiddlewareInstancePerResourceContainer(models, container));

        if (LOGGER.isEnabledFor(Level.INFO)) {
            LOGGER.info("Replace connectors with completions...");
        }
        
        final Configuration featureConfiguration = pcmModels.getFeatureConfig();
        
        Streams.stream(models.getSystem()
            .eAllContents())
            .filter(CompositionPackage.Literals.ASSEMBLY_CONNECTOR::isInstance)
            .map(AssemblyConnector.class::cast)
            .map(connector -> new ConnectorReplacingBuilder(models, connector, featureConfiguration.getDefaultConfig()))
            .forEach(IBuilder::build);
        
        postProcessCompletedMiddlewarePartition(completionsRepositoryPartition);
    }

    protected Repository getMiddlewareRepository() {
        ResourceSetPartition middlewareRepository = blackboard
            .getPartition(LoadMiddlewareConfigurationIntoBlackboardJob.RMI_MIDDLEWARE_PARTITION_ID);
        return middlewareRepository.<Repository> getElement(RepositoryPackage.Literals.REPOSITORY)
            .stream()
            .findAny()
            .orElseThrow(() -> new IllegalStateException(
                    "The middleware model is expected to contain a PCM Repository, but it does not."));
    }
    
    protected Repository createCompletionRepository(ResourceSetPartition completionRepositoryPartition, Repository middlewareRepository) throws JobFailedException {
        Repository completionRepository = RepositoryFactory.eINSTANCE.createRepository();
        completionRepository.setEntityName(String.format("CompletionRepository<%s>", middlewareRepository.getId()));
                
        var fileName = String.format("completion-%s.repository", middlewareRepository.getId());
        var completionRepositoryURI = getCompletionResourceURI(fileName);
        
        Resource r = completionRepositoryPartition.getResourceSet().createResource(completionRepositoryURI);
        r.getContents().add(completionRepository);
        
        return completionRepository;
    }
    
    protected ResourceSetPartition getPartitionForCompletedMiddleware() {
        ResourceSetPartition completionRepositoryPartition = new ResourceSetPartition();
        this.blackboard.addPartition(COMPLETION_REPOSITORY_PARTITION, completionRepositoryPartition);
        return completionRepositoryPartition;
    }
    
    protected void postProcessCompletedMiddlewarePartition(ResourceSetPartition partition) {
        try {
            partition.storeAllResources();
        } catch (final IOException e) {
            LOGGER.error("Unable to serialize the working copy of the completion models.", e);
        }
    }
    
    protected URI getCompletionResourceURI(String resourceName) throws JobFailedException {
        IFolder completionFolder = getOrCreateCompletionFolder();
        final URI completionFolderURI = URI.createFileURI(completionFolder.getLocation().toOSString());
        return completionFolderURI.appendSegment(resourceName);
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
    private void addOneMiddlewareInstancePerResourceContainer(PCMAndCompletionModelHolder models, ResourceContainer resContainer) {
        AssemblyContext ctx = CompositionFactory.eINSTANCE.createAssemblyContext();
        ctx.setEntityName("AssCtx Middleware " + resContainer.getEntityName() + " " + resContainer.getId());
        ctx.setEncapsulatedComponent__AssemblyContext(models.getMiddlewareRepository().getComponents__Repository()
                .get(0)); // TODO: Parameterise me!
        models.getSystem().getAssemblyContexts__ComposedStructure().add(ctx);

        models.getSystem().getAssemblyContexts__ComposedStructure().add(ctx);
        AllocationContext allocCtx = AllocationFactory.eINSTANCE.createAllocationContext();
        allocCtx.setEntityName("AllocCtx Middleware " + resContainer.getEntityName() + " " + resContainer.getId());
        allocCtx.setAssemblyContext_AllocationContext(ctx);
        allocCtx.setResourceContainer_AllocationContext(resContainer);
        models.getAllocation().getAllocationContexts_Allocation().add(allocCtx);

        if (LOGGER.isEnabledFor(Level.INFO)) {
            LOGGER.info("Added middleware component >"
                    + ctx.getEncapsulatedComponent__AssemblyContext().getEntityName() + "< to resource container >"
                    + resContainer.getEntityName() + " (" + resContainer.getId() + ")" + "<");
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
