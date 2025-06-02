package de.uka.ipd.sdq.codegen.simucontroller.core.dockmodel.events;

import de.uka.ipd.sdq.codegen.simucontroller.core.dockmodel.DockModel;

public class DockDeletedEvent extends DockEvent {

    public DockDeletedEvent(DockModel dock) {
        super(dock);
    }

}
