package de.uka.ipd.sdq.simucomframework.probes;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;

import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.probeframework.measurement.RequestContext;
import org.palladiosimulator.probeframework.probes.BasicObjectStateProbe;

import de.uka.ipd.sdq.simucomframework.resources.AbstractScheduledResource;

/**
 * Measures the CPU utilization (dimensionless) by requesting the queue length
 * from the scheduled CPU resource (observed state object) for all cores
 * normalized by the number of cores. If one core is busy with 2 processes and
 * another core is idle for a 2 core system then the utilization is 50%.
 * 
 * @author groenda
 */
public class TakeScheduledResourceUtilization extends
        BasicObjectStateProbe<AbstractScheduledResource, Double, Dimensionless> {

    /**
     * Default constructor.
     * 
     * @param scheduledResource
     *            The observer object is a scheduled resource, thus, allowing to request its queue
     *            length.
     */
    public TakeScheduledResourceUtilization(final AbstractScheduledResource scheduledResource) {
        super(scheduledResource, MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE);
    }

    /**
     * Measures the queue length from the scheduled resources (observed state object).
     */
    @Override
    protected Measure<Double, Dimensionless> getBasicMeasure(final RequestContext measurementContext) {
    	int allCores = getStateObject().getNumberOfInstances();
    	int utilizedCores = 0;
    	for (int coreNumber = 0; coreNumber < allCores; coreNumber++) {
    		if (getStateObject().getQueueLength(coreNumber) > 0) {
    			utilizedCores++;
    		}
    	}
    	
        return Measure.valueOf((double)utilizedCores/(double)allCores, Dimensionless.UNIT);
    }

}
