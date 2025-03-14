package de.uka.ipd.sdq.codegen.simucontroller.core.dockmodel.events;

import de.uka.ipd.sdq.codegen.simucontroller.core.dockmodel.DockModel;

public class DockSimTimeChangedEvent extends DockEvent {

    private double newSimTime;

    public DockSimTimeChangedEvent(DockModel dock, double simTime) {
        super(dock);
        this.newSimTime = simTime;
    }

    public double getNewSimTime() {
        return newSimTime;
    }

}
