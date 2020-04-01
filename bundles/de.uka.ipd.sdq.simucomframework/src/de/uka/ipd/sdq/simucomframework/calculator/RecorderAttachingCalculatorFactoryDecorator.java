package de.uka.ipd.sdq.simucomframework.calculator;

import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.probeframework.calculator.Calculator;
import org.palladiosimulator.probeframework.calculator.IGenericCalculatorFactory;
import org.palladiosimulator.probeframework.probes.ProbeConfiguration;
import org.palladiosimulator.recorderframework.IRecorder;
import org.palladiosimulator.recorderframework.config.AbstractRecorderConfiguration;
import org.palladiosimulator.recorderframework.config.IRecorderConfiguration;
import org.palladiosimulator.recorderframework.utils.RecorderExtensionHelper;

import de.uka.ipd.sdq.simucomframework.SimuComConfig;

/**
 * Factory class to create @see {@link Calculator}s used in a SimuCom simulation run.
 *
 * @author Steffen Becker, Philipp Merkle, Sebastian Lehrig
 */
public class RecorderAttachingCalculatorFactoryDecorator implements IGenericCalculatorFactory {
    /**
     * SimuCom model which is simulated
     */
    private final SimuComConfig configuration;

    private final IGenericCalculatorFactory decoratedCalculatorFactory;

    public RecorderAttachingCalculatorFactoryDecorator(final IGenericCalculatorFactory decoratedCalculatorFactory,
            final SimuComConfig configuration) {
        super();

        this.decoratedCalculatorFactory = decoratedCalculatorFactory;
        this.configuration = configuration;
    }

    @Override
    public Calculator buildCalculator(MetricDescription metric, MeasuringPoint measuringPoint,
    		ProbeConfiguration probeConfiguration) {
    	return setupRecorder(decoratedCalculatorFactory.buildCalculator(metric, measuringPoint, probeConfiguration));
    }

    private Calculator setupRecorder(final Calculator calculator) {
        final Map<String, Object> recorderConfigurationMap = new HashMap<String, Object>();
        recorderConfigurationMap.put(AbstractRecorderConfiguration.RECORDER_ACCEPTED_METRIC,
                calculator.getMetricDesciption());
        recorderConfigurationMap.put(AbstractRecorderConfiguration.MEASURING_POINT, calculator.getMeasuringPoint());

        final IRecorder recorder = RecorderExtensionHelper
                .instantiateRecorderImplementationForRecorder(this.configuration.getRecorderName());
        final IRecorderConfiguration recorderConfiguration = this.configuration.getRecorderConfigurationFactory()
                .createRecorderConfiguration(recorderConfigurationMap);
        recorder.initialize(recorderConfiguration);
        // register recorder at calculator
        calculator.addObserver(recorder);

        return calculator;
    }
}
