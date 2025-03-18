package de.uka.ipd.sdq.simucomframework.usage;

import org.palladiosimulator.commons.emfutils.EMFLoadHelper;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;
import de.uka.ipd.sdq.simucomframework.core.model.SimuComModel;
import de.uka.ipd.sdq.simucomframework.core.usage.IScenarioRunner;
import de.uka.ipd.sdq.simucomframework.core.usage.IUser;
import de.uka.ipd.sdq.simucomframework.core.usage.IUserFactory;

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
    public IUser createUser() {
        final IScenarioRunner scenarioRunner = this.createScenarioRunner();
        return new OpenWorkloadUser(model, "OpenUser", scenarioRunner, usageStartStopProbes, resourceTableManager);
    }

    /**
     * Template method filled in by the generator. Returns an object representing the user behaviour
     * needed for the new users.
     * 
     * @return The behaviour of the users created by this factory
     */
    public abstract IScenarioRunner createScenarioRunner();

}
