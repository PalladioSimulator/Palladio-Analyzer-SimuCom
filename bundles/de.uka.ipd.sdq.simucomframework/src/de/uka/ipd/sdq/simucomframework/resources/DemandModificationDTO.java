package de.uka.ipd.sdq.simucomframework.resources;

/**
 * @author Jonas Lehmann
 *
 */
public class DemandModificationDTO {

	private final double scaledDemand;

	private final double additiveDemandValue;

	public DemandModificationDTO(double scaledDemand, double additiveDemandValue) {
		this.scaledDemand = scaledDemand;
		this.additiveDemandValue = additiveDemandValue;
	}
	
	/**
	 * @return the scaled demand
	 */
	public double getScaledDemand() {
		return scaledDemand;
	}

	/**
	 * @return an additive demand of time units (delay, latency)
	 */
	public double getAdditiveDemandValue() {
		return additiveDemandValue;
	}
	
}
