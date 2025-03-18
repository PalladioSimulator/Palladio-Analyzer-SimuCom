/** */
package de.uka.ipd.sdq.simucomframework.core.usage;

/**
 * Interface for all workload drivers. A workload driver controls the simulated workload by
 * controlling simulated users of the system
 * 
 * @author Steffen Becker
 *
 */
public interface IWorkloadDriver {

    /**
     * Starts the workload
     */
    void run();

    IUserFactory getUserFactory();
}
