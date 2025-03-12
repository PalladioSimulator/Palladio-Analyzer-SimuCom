package de.uka.ipd.sdq.simucomframework.calculator;

import java.util.HashMap;
import java.util.Map;

import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.probeframework.calculator.Calculator;
import org.palladiosimulator.probeframework.calculator.CalculatorProbeSet;
import org.palladiosimulator.probeframework.calculator.IGenericCalculatorFactory;
import org.palladiosimulator.recorderframework.config.AbstractRecorderConfiguration;
import org.palladiosimulator.recorderframework.config.IRecorderConfigurationFactory;
import org.palladiosimulator.recorderframework.core.IRecorder;
import org.palladiosimulator.recorderframework.core.config.IRecorderConfiguration;
import org.palladiosimulator.recorderframework.utils.RecorderExtensionHelper;

/**
 * Factory class to create @see {@link Calculator}s used in a SimuCom simulation run.
 *
 * @author Steffen Becker, Philipp Merkle, Sebastian Lehrig
 */
public class RecorderAttachingCalculatorFactoryDecorator implements IGenericCalculatorFactory {
    /**
     * SimuCom model which is simulated
     */
    private final IGenericCalculatorFactory decoratedCalculatorFactory;
    private final String recorderName;
    private final IRecorderConfigurationFactory configurationFactory;
    
    public RecorderAttachingCalculatorFactoryDecorator(final IGenericCalculatorFactory decoratedCalculatorFactory,
            final String recorderName, IRecorderConfigurationFactory configurationFactory) {
        this.decoratedCalculatorFactory = decoratedCalculatorFactory;
        this.recorderName = recorderName;
        this.configurationFactory = configurationFactory;        
    }

    @Override
    public Calculator buildCalculator(MetricDescription metric, MeasuringPoint measuringPoint,
            CalculatorProbeSet probeConfiguration) {
        return setupRecorder(decoratedCalculatorFactory.buildCalculator(metric, measuringPoint, probeConfiguration));
    }

    private Calculator setupRecorder(final Calculator calculator) {
        final Map<String, Object> recorderConfigurationMap = new HashMap<String, Object>();
        recorderConfigurationMap.put(AbstractRecorderConfiguration.RECORDER_ACCEPTED_METRIC,
                calculator.getMetricDesciption());
        recorderConfigurationMap.put(AbstractRecorderConfiguration.MEASURING_POINT, calculator.getMeasuringPoint());

        final IRecorder recorder = RecorderExtensionHelper.instantiateRecorderImplementationForRecorder(recorderName);
        final IRecorderConfiguration recorderConfiguration = configurationFactory
            .createRecorderConfiguration(recorderConfigurationMap);
        recorder.initialize(recorderConfiguration);
        // register recorder at calculator
        calculator.addObserver(recorder);

        return calculator;
    }
}
