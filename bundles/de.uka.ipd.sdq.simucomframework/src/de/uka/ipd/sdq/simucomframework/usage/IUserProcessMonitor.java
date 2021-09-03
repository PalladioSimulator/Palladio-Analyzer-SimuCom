package de.uka.ipd.sdq.simucomframework.usage;

import de.uka.ipd.sdq.simulation.abstractsimengine.ISimProcess;

public interface IUserProcessMonitor {
    void registerProcess(ISimProcess process);
}
