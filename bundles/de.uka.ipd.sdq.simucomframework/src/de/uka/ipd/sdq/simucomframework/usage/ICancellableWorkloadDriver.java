package de.uka.ipd.sdq.simucomframework.usage;

import de.uka.ipd.sdq.simucomframework.core.usage.IWorkloadDriver;

/**
 * Extension of the Workload Driver Interface with capabilities to stop a workload
 * driver from executing a usage scenario.
 * 
 * @author Sebastian Krach
 *
 */
public interface ICancellableWorkloadDriver extends IWorkloadDriver {
    
    /**
     * Cancels the workload driver and prevents it from scheduling new demand.
     */
    public void cancel();
}
