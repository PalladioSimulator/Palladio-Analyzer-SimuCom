package de.uka.ipd.sdq.codegen.simucontroller.workflow.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.analyzer.workflow.blackboard.PCMResourceSetPartition;
import org.palladiosimulator.analyzer.workflow.configurations.AbstractPCMWorkflowRunConfiguration;
import org.palladiosimulator.analyzer.workflow.jobs.LoadPCMModelsIntoBlackboardJob;
import org.palladiosimulator.reliability.FailureStatistics;
import org.palladiosimulator.reliability.MarkovEvaluationType;
import org.palladiosimulator.reliability.helper.MarkovFailureTypeHelper;

import de.uka.ipd.sdq.codegen.simucontroller.runconfig.SimuComWorkflowConfiguration;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.MDSDBlackboard;

/**
 * Represents a step where the types of all possible failure-on-demand occurrences during the
 * simulation are determined.
 * 
 * @author brosch
 * 
 */
public class DetermineFailureTypesJob implements IBlackboardInteractingJob<MDSDBlackboard> {

    /**
     * The blackboard where the PCM model resides.
     */
    private MDSDBlackboard blackboard;

    /**
     * The configuration of the workflow.
     */
    private AbstractPCMWorkflowRunConfiguration configuration = null;

    /**
     * Provides functionality for managing failure types.
     */
    private final MarkovFailureTypeHelper helper = new MarkovFailureTypeHelper();

    /**
     * The constructor.
     * 
     * @param configuration
     *            the configuration of the workflow
     */
    public DetermineFailureTypesJob(final AbstractPCMWorkflowRunConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.uka.ipd.sdq.workflow.IJob#execute(org.eclipse.core.runtime. IProgressMonitor)
     */
    @Override
    public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {

        // Check for the "simulate failures" option:
        final SimuComWorkflowConfiguration config = (SimuComWorkflowConfiguration) configuration;
        if (!config.getSimulateFailures()) {
            return;
        }

        // Retrieve the PCM models that are already loaded into memory:
        final PCMResourceSetPartition pcmPartition = (PCMResourceSetPartition) this.blackboard
                .getPartition(LoadPCMModelsIntoBlackboardJob.PCM_MODELS_PARTITION_ID);

        // Derive the failure types for the simulation:
        FailureStatistics.setFailureTypes(helper.getFailureTypes(MarkovEvaluationType.POINTSOFFAILURE,
                pcmPartition.getRepositories(), pcmPartition.getResourceEnvironment(), pcmPartition.getSystem()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ipd.sdq.workflow.IJob#getName()
     */
    @Override
    public String getName() {
        return "Perform Failure Types Determination";
    }

    /*
     * (non-Javadoc)
     * 
     * @seede.uka.ipd.sdq.workflow.IJob#cleanup(org.eclipse.core.runtime. IProgressMonitor)
     */
    @Override
    public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ipd.sdq.workflow.IBlackboardInteractingJob#setBlackboard(de.uka
     * .ipd.sdq.workflow.Blackboard)
     */
    @Override
    public void setBlackboard(final MDSDBlackboard blackboard) {
        this.blackboard = blackboard;
    }
}
