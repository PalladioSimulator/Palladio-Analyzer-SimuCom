package de.uka.ipd.sdq.simucomframework.stopcondition;

import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.SI;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.measurementframework.listener.IMeasurementSourceListener;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;

import de.uka.ipd.sdq.simulation.abstractsimengine.SimCondition;
import de.uka.ipd.sdq.statistics.IBatchAlgorithm;
import de.uka.ipd.sdq.statistics.estimation.ConfidenceInterval;
import de.uka.ipd.sdq.statistics.estimation.IConfidenceEstimator;

/**
 * Provides a stop condition which determines when to stop based on the confidence interval around a
 * point estimation (e.g. mean).
 *
 * @author Philipp Merkle, Sebastian Lehrig
 *
 */
public class ConfidenceStopCondition implements SimCondition, IMeasurementSourceListener {

    private static final Logger LOGGER = Logger.getLogger(ConfidenceStopCondition.class);

    /** mean of the observations and the corresponding confidence interval */
    private ConfidenceInterval confidence;

    private boolean confidenceReached = false;

    private final IBatchAlgorithm batchAlgorithm;

    private final IConfidenceEstimator estimator;

    private final double confidenceLevel;

    private final double halfWidth;

    private int minBatches;

    /**
     *
     * @param batchAlgorithm
     * @param confidenceLevel
     *            the confidence level. Use values between 0 and 1.
     * @param halfWidth
     *            the relative half width of the target confidence interval. Use values between 0
     *            and 1.
     */
    public ConfidenceStopCondition(final IBatchAlgorithm batchAlgorithm,
            final IConfidenceEstimator estimator, final double confidenceLevel, final double halfWidth) {
        this.batchAlgorithm = batchAlgorithm;
        this.estimator = estimator;
        this.confidenceLevel = confidenceLevel;
        this.halfWidth = halfWidth;
        this.minBatches = 0;
    }

    @Override
    public boolean check() {
        return confidenceReached;
    }

    @Override
    public void newMeasurementAvailable(final MeasuringValue resultTuple) {
        final Measure<Double, Duration> responseTimeMeasure = resultTuple
                .getMeasureForMetric(MetricDescriptionConstants.RESPONSE_TIME_METRIC);
        final double responseTime = responseTimeMeasure.doubleValue(SI.SECOND);

        batchAlgorithm.offerSample(responseTime);
        if (batchAlgorithm.hasValidBatches() && batchAlgorithm.getBatchMeans().size() >= minBatches) {
            // estimate actual confidence interval
            final ConfidenceInterval ci = estimator.estimateConfidence(batchAlgorithm.getBatchMeans(), confidenceLevel);

            if (ci != null) {
                // construct target confidence interval
                final ConfidenceInterval targetCI = new ConfidenceInterval(ci.getMean(), halfWidth, confidenceLevel);

                if (targetCI.contains(ci)) {
                    if (LOGGER.isEnabledFor(Level.INFO)) {
                        LOGGER.info("Requested confidence reached.");
                    }
                    confidenceReached = true;
                    this.confidence = ci;

                    // request another batch in order to proceed with improving
                    // confidence interval's half-width until the simulation
                    // actually stops.
                    minBatches = batchAlgorithm.getBatchMeans().size() + 1;
                } else {
                    if (LOGGER.isEnabledFor(Level.INFO)) {
                        LOGGER.info("Requested confidence not yet reached.");
                    }

                    // request another batch in order to reduce the confidence
                    // interval's half-width
                    minBatches = batchAlgorithm.getBatchMeans().size() + 1;
                }
                if (LOGGER.isEnabledFor(Level.INFO)) {
                    LOGGER.info("Current confidence interval: Mean " + ci.getMean() + ", " + confidenceLevel * 100
                            + "% Confidence Interval " + "[" + ci.getLowerBound() + "," + ci.getUpperBound() + "]");
                }
            }
        }
    }

    public ConfidenceInterval getConfidence() {
        return confidence;
    }

    @Override
    public void preUnregister() {
        // TODO Auto-generated method stub
    }

}
