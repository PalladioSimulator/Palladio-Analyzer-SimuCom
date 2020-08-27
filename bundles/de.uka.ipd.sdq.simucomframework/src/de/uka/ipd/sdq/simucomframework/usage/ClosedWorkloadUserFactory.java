package de.uka.ipd.sdq.simucomframework.usage;

import org.palladiosimulator.commons.emfutils.EMFLoadHelper;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;

/**
 * Factory to create closed workload users
 * 
 * @author Steffen Becker, Sebastian Lehrig
 */
public abstract class ClosedWorkloadUserFactory extends AbstractWorkloadUserFactory implements IClosedWorkloadUserFactory {

    private final IResourceTableManager resourceTableManager;
    private String thinkTime;

    public ClosedWorkloadUserFactory(final SimuComModel model, final String thinkTimeSpec, final String usageScenarioURI, IResourceTableManager resourceTableManager) {
        this(model, thinkTimeSpec, (UsageScenario) EMFLoadHelper.loadAndResolveEObject(usageScenarioURI), resourceTableManager);
    }

    public ClosedWorkloadUserFactory(final SimuComModel model, final String thinkTimeSpec,
            final UsageScenario usageScenario, IResourceTableManager resourceTableManager) {
        super(model, usageScenario);
        this.resourceTableManager = resourceTableManager;
        this.thinkTime = thinkTimeSpec;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ipd.sdq.simucomframework.usage.IUserFactory#createUser()
     */
    @Override
    public ClosedWorkloadUser createUser() {
        final IScenarioRunner scenarioRunner = this.createScenarioRunner();
        return new ClosedWorkloadUser(model, "ClosedUser", scenarioRunner, thinkTime, usageStartStopProbes, resourceTableManager);
    }
    
    @Override
    public void setThinkTimeSpec(String thinkTimeSpec) {
        this.thinkTime = thinkTimeSpec;
    }

    /**
     * Template method filled by the generator. Returns the users behaviour.
     * 
     * @return The behaviour of the users created by this factory
     */
    public abstract IScenarioRunner createScenarioRunner();

}
