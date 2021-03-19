package de.uka.ipd.sdq.simucomframework.resources;

import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simucomframework.variables.converter.NumberConverter;

public class DemandModifyingBehavior {
	
	private final String scalingFactor; 	//of throughput or processingrate
    private final String delay; 			//or latency of resource
    
    public DemandModifyingBehavior(final String scalingFactor, final String delay) {
    	this.scalingFactor = scalingFactor;
    	this.delay = delay;
    }
	
	/**
	 * Calculates an additive demand of time units:
	 * additiveValue = prevDemand / scaleFactor + delay - prevDemand.
	 * 
	 * @param previousDemand demand value before modification
	 * @return an additive value to get the newDemand by adding it to the prevDemand
	 */
	public double getAdditiveDemandValue(double previousDemand) {
		final double scaling = NumberConverter.toDouble(StackContext.evaluateStatic(scalingFactor));
		if(scaling <= 0) {
			throw new RuntimeException("ScalingFactor of ressources processingrate/throughput was less or equal zero");
		}
		final double scaledDemand = previousDemand / scaling;
        final double additiveDemandValue = NumberConverter.toDouble(StackContext.evaluateStatic(delay)) + (scaledDemand - previousDemand);
		return additiveDemandValue;
	}
}
