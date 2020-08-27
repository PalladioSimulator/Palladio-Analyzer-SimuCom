package de.uka.ipd.sdq.simucomframework.resources;

import de.uka.ipd.sdq.scheduler.SchedulerModel;
import de.uka.ipd.sdq.scheduler.resources.active.AbstractActiveResource;
import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;
import de.uka.ipd.sdq.scheduler.resources.active.ResourceTableManager;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;

public abstract class SimuComExtensionResource extends AbstractActiveResource {

    public SimuComExtensionResource(SchedulerModel model, long capacity, String name, String id, ResourceTableManager resourceTableManager) {
        super(model, capacity, name, id, resourceTableManager);
    }

    public abstract void initialize(SimuComModel simuComModel);

}
