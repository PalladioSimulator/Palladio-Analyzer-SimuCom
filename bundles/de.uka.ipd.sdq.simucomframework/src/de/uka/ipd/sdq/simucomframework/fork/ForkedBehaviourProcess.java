package de.uka.ipd.sdq.simucomframework.fork;

import org.apache.log4j.Logger;

import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;
import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simucomframework.SimuComSimProcess;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimProcess;

/**
 * Base class for ForkBehaviours. Generator creates a specialisation of this and uses it to execute
 * actions in parallel
 *
 * @author Steffen Becker
 *
 */
public abstract class ForkedBehaviourProcess extends SimuComSimProcess {

    private static final Logger LOGGER = Logger.getLogger(ForkedBehaviourProcess.class.getName());

    protected final Context forkContext;
    protected final String assemblyContextID;
    
    private final ISimProcess parentProcess;
    private final boolean isAsync;
    private boolean isTerminated = false;


    public ForkedBehaviourProcess(Context context, String assemblyContextID, boolean isAsync, IResourceTableManager resourceTableManager) {
        super(context.getModel(), "Forked Behaviour", context.getThread().getRequestContext(), resourceTableManager);

        // use the session id from the parent process
        this.currentSessionId = context.getThread().getCurrentSessionId();

        forkContext = createForkContext(context);

        parentProcess = context.getThread();
        this.assemblyContextID = assemblyContextID;
        this.isAsync = isAsync;
    }

    /**
     * Factory method for the fork context used in the forked behaviour
     * 
     * @param context
     * @return
     */
    protected Context createForkContext(Context context) {
        return new ForkContext(context, this);
    }

    public ForkedBehaviourProcess(final Context context, final String assemblyContextID, final boolean isAsync,
            final int priority, IResourceTableManager resourceTableManager) {
        this(context, assemblyContextID, isAsync, resourceTableManager);
        setPriority(priority);
    }

    @Override
    protected void internalLifeCycle() {
        executeBehaviour();
        isTerminated = true;

        // if this has been synchronous call of the behaviour and the parent has
        // not yet terminated (which may happen under some wired conditions) and
        // the simulation is still running, we can think about triggering the
        // parent again.
        if (!isAsync && !parentProcess.isTerminated() && simulationIsRunning()) {
            parentProcess.scheduleAt(0);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Asynch behaviour finished at simtime " + getModel().getSimulationControl()
                    .getCurrentSimulationTime());
            }
        }
    }

    private boolean simulationIsRunning() {
        return forkContext.getModel().getSimulationControl().isRunning();
    }

    /**
     * Template method filled by the generate with the parallel behaviour specified in the PCM's
     * fork action
     */
    protected abstract void executeBehaviour();

    public boolean isAsync() {
        return isAsync;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.uka.ipd.sdq.simulation.abstractsimengine.SimProcess#isTerminated ()
     */
    @Override
    public boolean isTerminated() {
        return isTerminated;
    }

}
