package de.uka.ipd.sdq.simucomframework.resources;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simucomframework.variables.converter.NumberConverter;

/**
 * A DemandModifyingBehavior has a scalingFactor for the throughput or
 * processingrate of the resource and a value for the delay or latency.
 * AbstractScheduledResources use this behavior to modify the demand of time
 * units for a computation.
 * 
 * 
 * @author Jonas Lehmann
 *
 */
public class DemandModifyingBehavior {

	private final String scalingFactor; // of throughput or processingrate
	private final String delay; // or latency of resource

	/**
	 * Creates a new {@link DemandModifyingBehavior}.
	 * 
	 * @param scalingFactor value for scaling throughput or processingrate
	 * @param delay         value for delay or latency of the resource
	 */
	public DemandModifyingBehavior(final String scalingFactor, final String delay) {
		this.scalingFactor = scalingFactor;
		this.delay = delay;
	}

	/**
	 * Scales a demand with the scalingFactor (newDemand = prevDemand /
	 * scalingFactor). Returns the scaled demand and an additive demand of time
	 * units (delay, latency).
	 * 
	 * @param previousDemand
	 * @return A Demand Modification Data Transfer Object which store the two
	 *         values.
	 */
	public DemandModification modifyDemand(double previousDemand) {
		return new DemandModification(this.scaleDemand(previousDemand), this.getAdditiveDemandValue());
	}

	private double scaleDemand(double previousDemand) {
		final double scaling = NumberConverter.toDouble(StackContext.evaluateStatic(scalingFactor));
		if (scaling <= 0) {
			throw new RuntimeException("ScalingFactor of ressources processingrate/throughput was less or equal zero");
		}
		return previousDemand / scaling;
	}

	private double getAdditiveDemandValue() {
		return NumberConverter.toDouble(StackContext.evaluateStatic(delay));
	}
}
