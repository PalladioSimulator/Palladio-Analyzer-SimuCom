package de.uka.ipd.sdq.codegen.simucontroller.core.dockmodel.events;

import de.uka.ipd.sdq.codegen.simucontroller.core.dockmodel.DockModel;

public class DockAddedEvent extends DockEvent {

    public DockAddedEvent(DockModel dock) {
        super(dock);
    }
}
