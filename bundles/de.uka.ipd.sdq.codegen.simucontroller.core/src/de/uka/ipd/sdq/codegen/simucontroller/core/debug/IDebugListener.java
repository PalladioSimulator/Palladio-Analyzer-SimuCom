package de.uka.ipd.sdq.codegen.simucontroller.core.debug;

import de.uka.ipd.sdq.codegen.simucontroller.core.dockmodel.DockModel;

/**
 * TODO currently, the simucontroller.debug package is exported. Maybe, to hide the implementation,
 * move this interface together with a factory creating the debug listeners to a public package
 * 
 * @author hauck
 *
 */
public interface IDebugListener {

    public void simulationStartsInDock(DockModel dock);

    public void simulationStoppedInDock();

}
