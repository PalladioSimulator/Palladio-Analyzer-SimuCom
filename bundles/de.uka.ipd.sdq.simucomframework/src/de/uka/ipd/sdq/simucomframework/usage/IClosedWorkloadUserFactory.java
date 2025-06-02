package de.uka.ipd.sdq.simucomframework.usage;

import de.uka.ipd.sdq.simucomframework.core.usage.IUserFactory;

/**
 * Interface for factories creating closed workload users.
 * 
 * @author Dominik Werle
 * 
 */
public interface IClosedWorkloadUserFactory extends IUserFactory {
    ClosedWorkloadUser createUser();
    
    /**
     * Sets a new think time specification that is applied to newly created users.
     * @param thinkTimeSpec the new tink time specification. Must not be null.
     */
    void setThinkTimeSpec(String thinkTimeSpec);
}
