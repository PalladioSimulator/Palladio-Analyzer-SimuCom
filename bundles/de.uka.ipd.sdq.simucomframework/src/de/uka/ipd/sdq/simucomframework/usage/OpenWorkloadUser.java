package de.uka.ipd.sdq.simucomframework.usage;

import java.util.List;

import org.palladiosimulator.probeframework.probes.Probe;
import org.palladiosimulator.probeframework.probes.TriggeredProbe;

import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;
import de.uka.ipd.sdq.simucomframework.core.SimuComSimProcess;
import de.uka.ipd.sdq.simucomframework.core.exceptions.FailureException;
import de.uka.ipd.sdq.simucomframework.core.model.SimuComModel;
import de.uka.ipd.sdq.simucomframework.core.usage.IScenarioRunner;
import de.uka.ipd.sdq.simucomframework.core.usage.IUser;

/**
 * Base class for open workload users. Open workload users begin their life, execute their behaviour
 * once, and then they die
 * 
 * @author Steffen Becker
 * 
 */
public class OpenWorkloadUser extends SimuComSimProcess implements IUser {

    private final IScenarioRunner scenarioRunner;
    private final List<Probe> usageStartStopProbes;

    public OpenWorkloadUser(final SimuComModel owner, final String name, final IScenarioRunner scenarioRunner,
            final List<Probe> usageStartStopProbes, IResourceTableManager resourceTableManager) {
        super(owner, name, resourceTableManager);
        this.scenarioRunner = scenarioRunner;
        this.usageStartStopProbes = usageStartStopProbes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see desmoj.core.simulator.SimProcess#lifeCycle()
     */
    @Override
    protected void internalLifeCycle() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getName() + " started! I'm alive!!!");
        }
        // update session id
        updateNewSessionID();
        try {
            // TODO: Fixme and provide a new solution
            // blackboardGarbageCollector.enterRegion(getRequestContext()
            // .rootContext());
            ((TriggeredProbe) usageStartStopProbes.get(0)).takeMeasurement(getRequestContext());
            scenarioRunner(this);
            ((TriggeredProbe) usageStartStopProbes.get(1)).takeMeasurement(getRequestContext());
            if (getModel().getConfiguration().getSimulateFailures()) {
                getModel().getFailureStatistics().recordSuccess();
            }
        } catch (final FailureException exception) {
            if (getModel().getConfiguration().getSimulateFailures()) {
                getModel().getFailureStatistics().increaseUnhandledFailureCounter(exception.getFailureType(),
                        currentSessionId);
            }
        } finally {
            // Increase measurements counter manually as usage scenario run is
            // not finished:
            getModel().increaseMainMeasurementsCount();

            // TODO: Fixme and provide a new solution
            // blackboardGarbageCollector.leaveRegion(getRequestContext()
            // .rootContext());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(getName() + " done! I'm dying!!!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ipd.sdq.simucomframework.usage.IScenarioRunner#scenarioRunner(
     * desmoj.core.simulator.SimProcess)
     */
    @Override
    public void scenarioRunner(final SimuComSimProcess thread) {
        this.scenarioRunner.scenarioRunner(thread);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ipd.sdq.simucomframework.usage.IUser#startUserLife()
     */
    @Override
    public void startUserLife() {
        this.scheduleAt(0);
    }

}
