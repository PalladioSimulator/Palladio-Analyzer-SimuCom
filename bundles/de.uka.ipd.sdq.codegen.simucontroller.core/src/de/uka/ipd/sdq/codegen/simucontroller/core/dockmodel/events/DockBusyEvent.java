package de.uka.ipd.sdq.codegen.simucontroller.core.dockmodel.events;

import de.uka.ipd.sdq.codegen.simucontroller.core.dockmodel.DockModel;

public class DockBusyEvent extends DockEvent {

    public DockBusyEvent(DockModel dock) {
        super(dock);
    }

}
