package de.uka.ipd.sdq.simulation.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.palladiosimulator.analyzer.workflow.core.runconfig.ExperimentRunDescriptor;
import org.palladiosimulator.recorderframework.core.config.IRecorderConfigurationFactory;
import org.palladiosimulator.recorderframework.core.utils.RecorderExtensionHelper;

import de.uka.ipd.sdq.probfunction.math.IRandomGenerator;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationConfig;

/**
 * This is the abstract base class for simulation configurations. It encapsulates the configuration
 * elements that are common to all PCM simulators.
 *
 * @author roman
 * @author Philipp Merkle
 *
 */
public abstract class AbstractSimulationConfig implements Serializable, ISimulationConfig {

    private static final Logger LOGGER = Logger.getLogger(AbstractSimulationConfig.class);

    /** Serialization ID of this class. */
    private static final long serialVersionUID = 891323270372759718L;

    public static final String VARIATION_ID = "variationId";
    public static final String SIMULATOR_ID = "simulatorId";
    public static final String PERSISTENCE_RECORDER_NAME = "persistenceFramework";
    public static final String USE_FIXED_SEED = "useFixedSeed";
    public static final String FIXED_SEED_PREFIX = "fixedSeed";
    public static final String MAXIMUM_MEASUREMENT_COUNT = "maximumMeasurementCount";

    // Default values
    /** Default simulator implementation */
    public static final String DEFAULT_SIMULATOR_ID = "de.uka.ipd.sdq.codegen.simucontroller.simucom";
    /** Default name for an experiment run. */
    public static final String DEFAULT_EXPERIMENT_RUN = "MyRun";
    /** Default name for an experiment run. */
    public static final String DEFAULT_VARIATION_NAME = "Default Variation";
    /** Default for stop condition simulation time. */
    public static final String DEFAULT_SIMULATION_TIME = "150000";
    /** Default for stop condition maximum measurement count. */
    public static final String DEFAULT_MAXIMUM_MEASUREMENT_COUNT = "10000";
    /** Default name of persistence recorder. */
    public static final String DEFAULT_PERSISTENCE_RECORDER_NAME = "";

    public static final String VERBOSE_LOGGING = "verboseLogging";

    /** Simulation configuration tab */
    public static final String EXPERIMENT_RUN = "experimentRun";
    public static final String SIMULATION_TIME = "simTime";

    private final boolean verboseLogging;
    private final boolean isDebug;

    private final List<ISimulationListener> listeners;
    /** configuration options */
    protected String nameExperimentRun;
    protected String variationId;
    private String additionalExperimentRunDescription;
    protected long simuTime;
    protected Long maxMeasurementsCount;
    protected long[] randomSeed = null;
    protected IRandomGenerator randomNumberGenerator = null;
    protected String recorderName;
    protected IRecorderConfigurationFactory recorderConfigurationFactory;
    protected ExperimentRunDescriptor descriptor = null;
    private final String simulatorId;

    /**
     * Constructs a new AbstractSimulationConfig.
     * 
     * This constructor initializes the RecorderFramework (legacy).
     * 
     * @param configuration
     *            a map which maps configuration option IDs to their values.
     */
    public AbstractSimulationConfig(final Map<String, Object> configuration, final boolean debug) {
        this(configuration, debug, null);
        this.recorderConfigurationFactory = RecorderExtensionHelper
            .getRecorderConfigurationFactoryForName(this.recorderName);
        this.recorderConfigurationFactory.initialize(configuration);
    }

    /**
     * Constructs a new AbstractSimulationConfig.
     * 
     * This constructor allows to circumvent the inflexible instantiation and initialization of
     * IRecorderConfigurationFactory as part of the constructor.
     * 
     * This constructor does not initialize the RecorderFramework.
     */
    public AbstractSimulationConfig(final Map<String, Object> configuration, final boolean debug,
            IRecorderConfigurationFactory configFactory) {
        this.verboseLogging = (Boolean) configuration.get(VERBOSE_LOGGING);
        this.isDebug = debug;
        this.variationId = (String) configuration.get(VARIATION_ID);

        this.simulatorId = (String) configuration.get(SIMULATOR_ID);
        this.nameExperimentRun = (String) configuration.get(EXPERIMENT_RUN);
        this.simuTime = Long.valueOf((String) configuration.get(SIMULATION_TIME));
        this.maxMeasurementsCount = Long.valueOf((String) configuration.get(MAXIMUM_MEASUREMENT_COUNT));
        this.randomSeed = getSeedFromConfig(configuration);

        this.recorderName = (String) configuration.get(PERSISTENCE_RECORDER_NAME);

        this.recorderConfigurationFactory = configFactory;

        this.listeners = new ArrayList<>();
    }

    /**
     * @return the recorderConfigurationFactory
     */
    public final IRecorderConfigurationFactory getRecorderConfigurationFactory() {
        return this.recorderConfigurationFactory;
    }

    public boolean getVerboseLogging() {
        return this.verboseLogging || this.isDebug;
    }

    public boolean isDebug() {
        return this.isDebug;
    }

    public void addListener(final ISimulationListener l) {
        this.listeners.add(l);
    }

    public List<ISimulationListener> getListeners() {
        return this.listeners;
    }

    public String getAdditionalExperimentRunDescription() {
        return this.additionalExperimentRunDescription;
    }

    public void setAdditionalExperimentRunDescription(final String additionalExperimentRunDescription) {
        this.additionalExperimentRunDescription = additionalExperimentRunDescription;
    }

    protected long[] getSeedFromConfig(final Map<String, Object> configuration) {
        if ((Boolean) configuration.get(USE_FIXED_SEED)) {
            final long[] seed = new long[6];
            for (int i = 0; i < 6; i++) {
                seed[i] = Long.parseLong((String) configuration.get(FIXED_SEED_PREFIX + i));
            }
            return seed;
        }
        return null;
    }

    @Override
    public String getNameExperimentRun() {
        String name = "";
        if (this.descriptor != null) {
            name += this.descriptor.getNameExperimentRun();
        } else {
            name += getNameBase();
        }
        if (this.additionalExperimentRunDescription != null) {
            name += this.additionalExperimentRunDescription;
        }
        return name;
    }

    public String getNameBase() {
        return this.nameExperimentRun;
    }

    public String getVariationId() {
        return this.variationId;
    }

    public void setNameBase(final String name) {
        this.nameExperimentRun = name;
    }

    public long getSimuTime() {
        return this.simuTime;
    }

    public long getMaxMeasurementsCount() {
        return this.maxMeasurementsCount;
    }

    public String getRecorderName() {
        return this.recorderName;
    }

    public String getEngine() {
        return "org.palladiosimulator.simulation.abstractsimengine.ssj.SSJSimEngineFactory";
    }

    /**
     * Dispose random generator and delete reference to it so that this {@link SimuComConfig} can be
     * started again and will create a new RandomGenerator.
     *
     * @author martens
     */
    public void disposeRandomGenerator() {
        this.randomNumberGenerator.dispose();
        this.randomNumberGenerator = null;
    }

    public void setExperimentRunDescriptor(final ExperimentRunDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public ExperimentRunDescriptor getExperimentRunDescriptor() {
        return this.descriptor;
    }

    public String getSimulatorId() {
        return this.simulatorId;
    }

}
