package de.uka.ipd.sdq.simucomframework.usage;

import org.palladiosimulator.commons.emfutils.EMFLoadHelper;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;

/**
 * A factory for creating open workload users
 * 
 * @author Steffen Becker
 * 
 */
public abstract class OpenWorkloadUserFactory extends AbstractWorkloadUserFactory implements IUserFactory {
    
    private final IResourceTableManager resourceTableManager;

    public OpenWorkloadUserFactory(final SimuComModel model, final String usageScenarioURI, IResourceTableManager resourceTableManager) {
        this(model, (UsageScenario) EMFLoadHelper.loadAndResolveEObject(usageScenarioURI), resourceTableManager);
    }

    public OpenWorkloadUserFactory(final SimuComModel model, UsageScenario usageScenario, IResourceTableManager resourceTableManager) {
        super(model, usageScenario);
        this.resourceTableManager = resourceTableManager;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ipd.sdq.simucomframework.usage.IUserFactory#createUser()
     */
    @Override
    public IUser createUser(IUserProcessMonitor processMonitor) {
        final IScenarioRunner scenarioRunner = this.createScenarioRunner();
        OpenWorkloadUser openWorkloadUser = new OpenWorkloadUser(model, "OpenUser", scenarioRunner, usageStartStopProbes, resourceTableManager);
        if (processMonitor != null) {
            processMonitor.registerProcess(openWorkloadUser);
        }
        openWorkloadUser.startProcess();
        return openWorkloadUser;
    }

    /**
     * Template method filled in by the generator. Returns an object representing the user behaviour
     * needed for the new users.
     * 
     * @return The behaviour of the users created by this factory
     */
    public abstract IScenarioRunner createScenarioRunner();

}
