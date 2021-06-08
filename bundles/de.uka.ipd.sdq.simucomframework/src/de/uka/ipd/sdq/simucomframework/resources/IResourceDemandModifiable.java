package de.uka.ipd.sdq.simucomframework.resources;

/**
 * Interface of AbstractScheduledResources to add and remove DemandModifyingBehaviors.
 * 
 * @author Jonas Lehmann
 *
 */
public interface IResourceDemandModifiable {
	
	/**
	 * Adds a DemandModifyingBehavior which can be used during the demand calculation.
	 * 
	 * @param behavior the DemandModifyingBehavior
	 */
	public void addDemandModifyingBehavior(final DemandModifyingBehavior behavior);

    /**
     * Removes a DemandModifyingBehavior.
     * 
     * @param behavior the DemandModifyingBehavior
     */
    public void removeDemandModifyingBehavior(final DemandModifyingBehavior behavior);
}
