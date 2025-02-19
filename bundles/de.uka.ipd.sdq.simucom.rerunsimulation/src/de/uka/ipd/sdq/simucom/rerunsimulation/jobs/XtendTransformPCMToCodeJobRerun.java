package de.uka.ipd.sdq.simucom.rerunsimulation.jobs;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.mwe2.runtime.workflow.Workflow;
import org.palladiosimulator.analyzer.workflow.core.blackboard.PCMResourceSetPartition;
import org.palladiosimulator.analyzer.workflow.jobs.LoadPCMModelsIntoBlackboardJob;

import de.uka.ipd.sdq.codegen.simucontroller.workflow.jobs.xtendworkflow.AllocationWorkflowComponent;
import de.uka.ipd.sdq.codegen.simucontroller.workflow.jobs.xtendworkflow.SystemWorkflowComponent;
import de.uka.ipd.sdq.codegen.simucontroller.workflow.jobs.xtendworkflow.UsageModelWorkflowComponent;
import de.uka.ipd.sdq.simucom.rerunsimulation.runconfig.RerunSimuComWorkflowConfiguration;
import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.IBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.IJob;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.SequentialBlackboardInteractingJob;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;
import de.uka.ipd.sdq.workflow.mdsd.blackboard.MDSDBlackboard;

/**
 * Start the Workflow-Engine of oAW - Generator. Only system, allocation and usage can be
 * regenerated in this job.
 */
public class XtendTransformPCMToCodeJobRerun extends SequentialBlackboardInteractingJob<MDSDBlackboard> implements
        IJob, IBlackboardInteractingJob<MDSDBlackboard> {

    private RerunSimuComWorkflowConfiguration configuration = null;

    public XtendTransformPCMToCodeJobRerun(RerunSimuComWorkflowConfiguration configuration) {
        super();

        this.configuration = configuration;
    }

    public void cleanup(IProgressMonitor monitr) throws CleanupFailedException {
        // do nothing
    }

    @Override
    public void execute(IProgressMonitor monitor) throws JobFailedException, UserCanceledException {

        Workflow workflow = new Workflow();
        final Map<String, Object> systemTransformationSlots = getSystemTransformationSlots();

        // Regenerate allocation, system and/or usage code in case the user wants this
        if (configuration.isRegenerateSystem()) {
            workflow.addComponent(new SystemWorkflowComponent(systemTransformationSlots, getBasePath()));
        }
        if (configuration.isRegenerateAllocation() || configuration.isRegenerateSystem()) {
            workflow.addComponent(new AllocationWorkflowComponent(systemTransformationSlots, getBasePath()));
        }
        if (configuration.isRegenerateUsage()) {
            workflow.addComponent(new UsageModelWorkflowComponent(systemTransformationSlots, getBasePath()));
        }

        // Now let them run
        workflow.invoke(null);
    }

    /**
     * 
     * @return a map containing the system transformation slots (system allocation, usage)
     */
    private HashMap<String, Object> getSystemTransformationSlots() {
        HashMap<String, Object> sC2 = new HashMap<String, Object>();
        PCMResourceSetPartition pcmPartition = (PCMResourceSetPartition) this.myBlackboard
                .getPartition(LoadPCMModelsIntoBlackboardJob.PCM_MODELS_PARTITION_ID);

        sC2.put("system", pcmPartition.getSystem());
        sC2.put("allocation", pcmPartition.getAllocation());
        sC2.put("usage", pcmPartition.getUsageModel());

        return sC2;
    }

    /**
     * 
     * @return the base path for the plug in that is generated
     */
    private String getBasePath() {
        String basePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString() + "/"
                + this.configuration.getStoragePluginID() + "/" + "src";

        return basePath;
    }

    public String getName() {
        return "Regenerate SimuCom Plugin Code";
    }
}