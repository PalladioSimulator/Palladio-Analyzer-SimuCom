package de.uka.ipd.sdq.simucomframework.resources;

public interface IResourceDemandModifiable {
	
	public void addDemandModifyingBehavior(final DemandModifyingBehavior behavior);

    public void removeDemandModifyingBehavior(final DemandModifyingBehavior behavior);
}
