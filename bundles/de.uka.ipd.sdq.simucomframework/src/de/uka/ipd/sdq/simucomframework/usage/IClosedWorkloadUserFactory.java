package de.uka.ipd.sdq.simucomframework.usage;

/**
 * Interface for factories creating closed workload users.
 * 
 * @author Dominik Werle
 * 
 */
public interface IClosedWorkloadUserFactory extends IUserFactory {
    ClosedWorkloadUser createUser(IUserProcessMonitor processMonitor);
    
    /**
     * Sets a new think time specification that is applied to newly created users.
     * @param thinkTimeSpec the new tink time specification. Must not be null.
     */
    void setThinkTimeSpec(String thinkTimeSpec);
}
