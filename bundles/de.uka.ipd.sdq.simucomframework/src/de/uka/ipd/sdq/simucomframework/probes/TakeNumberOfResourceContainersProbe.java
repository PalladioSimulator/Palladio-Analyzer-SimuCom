package de.uka.ipd.sdq.simucomframework.probes;

import javax.measure.Measure;
import javax.measure.quantity.Dimensionless;

import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.probeframework.measurement.RequestContext;
import org.palladiosimulator.probeframework.probes.BasicObjectStateProbe;

/**
 * Probes the number of resource containers within a resource environment.
 * 
 * @author Sebastian Lehrig
 */
public class TakeNumberOfResourceContainersProbe
        extends BasicObjectStateProbe<ResourceEnvironment, Long, Dimensionless> {

    /**
     * Default constructor.
     * 
     * @param resourceRegistry
     *            The observer object is a ResourceEnvironment, thus, allowing to request the
     *            current number of included resource containers.
     */
    public TakeNumberOfResourceContainersProbe(final ResourceEnvironment resourceEnvironment) {
        super(resourceEnvironment, MetricDescriptionConstants.NUMBER_OF_RESOURCE_CONTAINERS);
    }

    /**
     * Measures the current number of resource containers the ResourceEnvironment (observed state
     * object).
     */
    @Override
    protected Measure<Long, Dimensionless> getBasicMeasure(final RequestContext measurementContext) {
        return Measure.valueOf(new Long(getStateObject().getResourceContainer_ResourceEnvironment().size()),
                Dimensionless.UNIT);
    }

}
