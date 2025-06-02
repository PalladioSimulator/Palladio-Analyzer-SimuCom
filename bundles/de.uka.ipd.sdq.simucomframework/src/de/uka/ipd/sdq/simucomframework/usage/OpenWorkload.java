package de.uka.ipd.sdq.simucomframework.usage;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;
import de.uka.ipd.sdq.simucomframework.core.Context;
import de.uka.ipd.sdq.simucomframework.core.SimuComSimProcess;
import de.uka.ipd.sdq.simucomframework.core.model.SimuComModel;
import de.uka.ipd.sdq.simucomframework.core.usage.IUser;
import de.uka.ipd.sdq.simucomframework.core.usage.IUserFactory;

/**
 * Implementation of the workload driver interface for open workloads
 *
 * @author Steffen Becker
 *
 */
public class OpenWorkload extends SimuComSimProcess implements ICancellableWorkloadDriver {

    private String interArrivalTime;
    private final IUserFactory userFactory;
    private boolean cancelled = false;

    private static final Logger LOGGER = Logger.getLogger(OpenWorkload.class.getName());

    /**
     * Counter for usage scenario runs.
     */

    /**
     * Constructor of the open workload driver
     *
     * @param model
     *            The simulation model this driver belongs to
     * @param userFactory
     *            The factory which is used to bread the users
     * @param interArrivalTime
     *            The time to wait between leaving a new user to its fate
     */
    public OpenWorkload(final SimuComModel model, final IUserFactory userFactory, final String interArrivalTime, IResourceTableManager resourceTableManager) {
        super(model, "OpenWorkloadUserMaturationChamber", resourceTableManager);
        this.interArrivalTime = interArrivalTime;
        this.userFactory = userFactory;
    }

    @Override
    public void run() {
        cancelled = false;
        this.scheduleAt(0);
    }
    
    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    protected void internalLifeCycle() {

        // As long as the simulation is running, new OpenWorkloadUsers are
        // generated and started:
        while (getModel().getSimulationControl().isRunning() && !this.cancelled) {

            try {
                // Generate and execute the new user:
                generateUser();

                // Wait for inter-arrival time:
                waitForNextUser();

                // Count the new user:
                if (this.getModel().getConfiguration().getSimulateFailures()) {
                    this.getModel().getFailureStatistics().increaseRunCount();
                    this.getModel().getFailureStatistics()
                            .printRunCount(LOGGER, getModel().getSimulationControl().getCurrentSimulationTime());
                }
            } catch (final OutOfMemoryError e) {
                // the system is overloaded. stop simulation
                if (LOGGER.isEnabledFor(Level.INFO)) {
                    LOGGER.info("Stopping simulation run due to memory constraints.");
                }
                getModel().getSimulationControl().stop();
            }
        }

        // Print failure statistics:
        if (this.getModel().getConfiguration().getSimulateFailures()) {
            this.getModel()
                    .getFailureStatistics()
                    .printHandledFailuresStatistics(LOGGER,
                            this.getModel().getSimulationControl().getCurrentSimulationTime());
        }
    }

    private void waitForNextUser() {
        final double interArrivalTimeSample = Context.evaluateStatic(interArrivalTime, Double.class);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Waiting for " + interArrivalTimeSample + " before spawing the next user");
        }
        this.hold(interArrivalTimeSample);
    }

    private IUser generateUser() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Spawning New User...");
        }
        final IUser user = userFactory.createUser();
        user.startUserLife();
        return user;
    }

    @Override
    public IUserFactory getUserFactory() {
        return this.userFactory;
    }

    public void setInterarrivalTime(final String newInterarrivalTime) {
        this.interArrivalTime = newInterarrivalTime;
    }

}
