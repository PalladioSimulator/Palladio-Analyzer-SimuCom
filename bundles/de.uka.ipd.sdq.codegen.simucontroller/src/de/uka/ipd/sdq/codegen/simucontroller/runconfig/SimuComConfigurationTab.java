/**
 *
 */
package de.uka.ipd.sdq.codegen.simucontroller.runconfig;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.palladiosimulator.analyzer.workflow.ConstantsContainer;
import org.palladiosimulator.editors.commons.dialogs.selection.PalladioSelectEObjectDialog;
import org.palladiosimulator.pcm.ui.provider.PalladioItemProviderAdapterFactory;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.provider.UsagemodelItemProviderAdapterFactory;
import org.palladiosimulator.recorderframework.launch.RecorderTabGroup;
import org.palladiosimulator.recorderframework.utils.RecorderExtensionHelper;

import de.uka.ipd.sdq.codegen.simucontroller.SimuControllerImages;
import de.uka.ipd.sdq.simucomframework.SimuComConfig;
import de.uka.ipd.sdq.simulation.AbstractSimulationConfig;
import de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper;

/**
 * The class defines a tab, which is responsible for the SimuCom configuration.
 *
 * @author Roman Andrej
 */
public class SimuComConfigurationTab extends AbstractLaunchConfigurationTab {

    /** Logger of this class. */
    private static final Logger LOGGER = Logger.getLogger(SimuComConfigurationTab.class);

    private Text nameField;
    private Text variationField;
    private Text timeField;
    private Text maxMeasurementsField;
    private Button checkLoggingButton;

    /** Confidence settings */
    private Button useConfidenceCheckBox;
    private Label levelLabel;
    private Text levelField;
    private Label halfWidthLabel;
    private Text halfWidthField;
    private Label selectModelElementLabel;
    private Text selectModelElementField;
    private Button selectModelElementButton;
    private String selectedModelElementName;
    private URI selectedModelElementURI;
    /** Batch means settings */
    private Button useAutomatedBatchMeansCheckBox;
    private Label batchSizeLabel;
    private Text batchSizeField;
    private Label minNumberOfBatchesLabel;
    private Text minNumberOfBatchesField;
    // Random Number Generator Seed(s)
    private Button fixedSeedButton;
    private Text[] seedText;

    private Combo persistenceCombo;
    private Combo simulatorCombo;
    private final ArrayList<String> modelFiles = new ArrayList<String>();

    private RecorderTabGroup recorderTabGroup;
    protected Composite container;

    public SimuComConfigurationTab() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(final Composite parent) {
        final ModifyListener modifyListener = new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                SimuComConfigurationTab.this.setDirty(true);
                SimuComConfigurationTab.this.updateLaunchConfigurationDialog();
            }
        };

        this.container = new Composite(parent, SWT.NONE);
        this.container.setLayout(new GridLayout());
        setControl(this.container);

        /** Create Simulator section */
        createSimulatorGroup();

        /** Create Experiment Run section */
        createExperimentRunGroup(modifyListener);

        /** DataSet group */
        createDataSetGroup();

        /** Create Stop Conditions section */
        createStopConditionGroup(modifyListener);

        /** Create Confidence Stop Condition section */
        createConfidenceStopConditionGroup(modifyListener);

        /** Logging group */
        createLoggingGroup();

        /** Generator Seeds group */
        createGeneratorSeedsGroup(modifyListener);
    }

    /**
     * Creates the simulator group, the first section of the SimuComConfigurationTab
     */
    protected void createSimulatorGroup() {
        final Group simulatorGroup = new Group(this.container, SWT.NONE);
        simulatorGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        final GridLayout simulatorLayout = new GridLayout();
        simulatorLayout.numColumns = 3;
        simulatorGroup.setLayout(simulatorLayout);
        simulatorGroup.setText("Simulator");

        final Label simulatorLabel = new Label(simulatorGroup, SWT.NONE);
        simulatorLabel.setText("Simulator implementation:");

        String[] simulatorNames = null;
        try {
            simulatorNames = SimulatorExtensionHelper.getSimulatorNames();
        } catch (final CoreException e1) {
            if (LOGGER.isEnabledFor(Level.WARN)) {
                LOGGER.warn("Could not retrieve names of simulator extensions.", e1);
            }
        }
        this.simulatorCombo = new Combo(simulatorGroup, SWT.READ_ONLY);
        this.simulatorCombo.setItems(simulatorNames);
        this.simulatorCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        this.simulatorCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    /**
     * Creates the experiment run group, the second section of the SimuComConfigurationTab
     * 
     * @param modifyListener
     *            the ModifyListener which should be added to the elements
     */
    protected void createExperimentRunGroup(final ModifyListener modifyListener) {
        final Group experimentrunGroup = new Group(this.container, SWT.NONE);
        experimentrunGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        experimentrunGroup.setLayout(gridLayout);
        experimentrunGroup.setText("Experiment Run");

        final Label nameLabel = new Label(experimentrunGroup, SWT.NONE);
        nameLabel.setText("Experiment Name:");

        this.nameField = new Text(experimentrunGroup, SWT.BORDER);
        final GridData gd_nameField = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_nameField.widthHint = 70;
        this.nameField.setLayoutData(gd_nameField);
        this.nameField.addModifyListener(modifyListener);
        
        final Label variationLabel = new Label(experimentrunGroup, SWT.NONE);
        variationLabel.setText("Variation Name:");
        
        this.variationField = new Text(experimentrunGroup, SWT.BORDER);
        final GridData gd_variationField = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_variationField.widthHint = 70;
        this.variationField.setLayoutData(gd_variationField);
        this.variationField.addModifyListener(modifyListener);
    }

    /**
     * Creates the data set group, the third group of the SimuComConfigurationTab
     */
    protected void createDataSetGroup() {
        final Group persistenceGroup = new Group(this.container, SWT.NONE);
        persistenceGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        persistenceGroup.setLayout(new GridLayout(2, false));
        persistenceGroup.setText("Simulation Results");

        final Label persistenceLabel = new Label(persistenceGroup, SWT.NONE);
        persistenceLabel.setText("Persistence Framework:");

        final List<String> recorderNames = RecorderExtensionHelper.getRecorderNames();
        this.persistenceCombo = new Combo(persistenceGroup, SWT.READ_ONLY);
        this.persistenceCombo.setItems(recorderNames.toArray(new String[recorderNames.size()]));
        this.persistenceCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        this.persistenceCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        this.recorderTabGroup = new RecorderTabGroup();
        final CTabFolder tabFolder = TabHelper.createTabFolder(this.recorderTabGroup, getLaunchConfigurationDialog(),
                getLaunchConfigurationDialog().getMode(), persistenceGroup, SWT.BORDER | SWT.FLAT);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
    }

    /**
     * Creates the stop condition group, the fourth section of the SimuComConfigurationTab
     * 
     * @param modifyListener
     *            the ModifyListener which should be added to the elements
     */
    protected void createStopConditionGroup(final ModifyListener modifyListener) {
        final Group stopConditionsGroup = new Group(this.container, SWT.NONE);
        stopConditionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 3;
        stopConditionsGroup.setLayout(gridLayout_1);
        stopConditionsGroup.setText("Stop Conditions");

        final Label timeLabel = new Label(stopConditionsGroup, SWT.NONE);
        timeLabel.setText("Maximum simulation time:");

        this.timeField = new Text(stopConditionsGroup, SWT.BORDER);
        this.timeField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        this.timeField.addModifyListener(modifyListener);

        final Label secLabel = new Label(stopConditionsGroup, SWT.NONE);
        secLabel.setText("Simulated Time Units");

        final Label maxLabel = new Label(stopConditionsGroup, SWT.NONE);
        maxLabel.setText("Maximum measurements count:");

        this.maxMeasurementsField = new Text(stopConditionsGroup, SWT.BORDER);
        this.maxMeasurementsField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        this.maxMeasurementsField.addModifyListener(modifyListener);
    }

    /**
     * The confidence stop condition group, the fifth group of the SimuComConfigurationTab
     * 
     * @param modifyListener
     *            the ModifyListener which should be added to the elements
     */
    protected void createConfidenceStopConditionGroup(final ModifyListener modifyListener) {
        final Group confidenceGroup = new Group(this.container, SWT.NONE);
        confidenceGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        final GridLayout confidenceLayout = new GridLayout();
        confidenceLayout.numColumns = 3;
        confidenceGroup.setLayout(confidenceLayout);
        confidenceGroup.setText("Confidence Stop Condition");

        this.useConfidenceCheckBox = new Button(confidenceGroup, SWT.CHECK);
        this.useConfidenceCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        this.useConfidenceCheckBox.setText("Stop when reaching confidence");
        this.useConfidenceCheckBox.setSelection(false);
        this.useConfidenceCheckBox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                // enable level and half-with fields if and only if check box is checked
                final boolean selected = SimuComConfigurationTab.this.useConfidenceCheckBox.getSelection();
                SimuComConfigurationTab.this.levelLabel.setEnabled(selected);
                SimuComConfigurationTab.this.levelField.setEnabled(selected);
                SimuComConfigurationTab.this.halfWidthLabel.setEnabled(selected);
                SimuComConfigurationTab.this.halfWidthField.setEnabled(selected);
                SimuComConfigurationTab.this.selectModelElementLabel.setEnabled(selected);
                SimuComConfigurationTab.this.selectModelElementField.setEnabled(selected);
                SimuComConfigurationTab.this.selectModelElementButton.setEnabled(selected);
                enableBatchMeansSettings(selected);

                SimuComConfigurationTab.this.updateLaunchConfigurationDialog();
            }

        });

        this.levelLabel = new Label(confidenceGroup, SWT.NONE);
        this.levelLabel.setText("Confidence level (%):");
        this.levelLabel.setEnabled(false);

        this.levelField = new Text(confidenceGroup, SWT.BORDER);
        this.levelField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        this.levelField.addModifyListener(modifyListener);
        this.levelField.setEnabled(false);

        this.halfWidthLabel = new Label(confidenceGroup, SWT.NONE);
        this.halfWidthLabel.setText("Confidence interval half-width (%):");
        this.halfWidthLabel.setEnabled(false);

        this.halfWidthField = new Text(confidenceGroup, SWT.BORDER);
        this.halfWidthField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        this.halfWidthField.addModifyListener(modifyListener);
        this.halfWidthField.setEnabled(false);

        this.selectModelElementLabel = new Label(confidenceGroup, SWT.NONE);
        this.selectModelElementLabel.setText("Monitor Response Time of:");
        this.selectModelElementLabel.setEnabled(false);

        this.selectModelElementField = new Text(confidenceGroup, SWT.BORDER);
        this.selectModelElementField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        this.selectModelElementField.addModifyListener(modifyListener);
        this.selectModelElementField.setEditable(false);
        this.selectModelElementField.setEnabled(false);

        this.selectModelElementButton = new Button(confidenceGroup, SWT.NONE);
        this.selectModelElementButton.setText("Select Model Element...");
        this.selectModelElementButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                showSelectModelElementDialog();
            }
        });

        /** Batch means configuration */
        this.useAutomatedBatchMeansCheckBox = new Button(confidenceGroup, SWT.CHECK);
        this.useAutomatedBatchMeansCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        this.useAutomatedBatchMeansCheckBox.setText(
                "Automatically determine batch size (Beware: Manual batch size can lead to invalid results, only use it care).");
        this.useAutomatedBatchMeansCheckBox.setSelection(false);
        this.useAutomatedBatchMeansCheckBox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                // enable level and half-with fields if and only if check box is checked
                final boolean selected = SimuComConfigurationTab.this.useAutomatedBatchMeansCheckBox.getSelection();
                SimuComConfigurationTab.this.batchSizeLabel.setEnabled(!selected);
                SimuComConfigurationTab.this.batchSizeField.setEnabled(!selected);
                SimuComConfigurationTab.this.minNumberOfBatchesLabel.setEnabled(!selected);
                SimuComConfigurationTab.this.minNumberOfBatchesField.setEnabled(!selected);

                SimuComConfigurationTab.this.updateLaunchConfigurationDialog();
            }
        });

        this.batchSizeLabel = new Label(confidenceGroup, SWT.NONE);
        this.batchSizeLabel.setText("Batch size:");
        this.batchSizeLabel.setEnabled(false);

        this.batchSizeField = new Text(confidenceGroup, SWT.BORDER);
        this.batchSizeField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        this.batchSizeField.addModifyListener(modifyListener);
        this.batchSizeField.setEnabled(false);

        this.minNumberOfBatchesLabel = new Label(confidenceGroup, SWT.NONE);
        this.minNumberOfBatchesLabel.setText("Minimum number of batches:");
        this.minNumberOfBatchesLabel.setEnabled(false);

        this.minNumberOfBatchesField = new Text(confidenceGroup, SWT.BORDER);
        this.minNumberOfBatchesField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        this.minNumberOfBatchesField.addModifyListener(modifyListener);
        this.minNumberOfBatchesField.setEnabled(false);
    }

    /**
     * The logging group, the sixth section of the SimuComConfigurationTab
     */
    protected void createLoggingGroup() {
        final Group loggingGroup = new Group(this.container, SWT.NONE);
        loggingGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        loggingGroup.setText("Logging");
        loggingGroup.setLayout(new GridLayout());
        this.checkLoggingButton = new Button(loggingGroup, SWT.CHECK);
        this.checkLoggingButton.setText("Enable verbose logging");
        this.checkLoggingButton.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.
             * SelectionEvent)
             */
            @Override
            public void widgetSelected(final SelectionEvent e) {
                SimuComConfigurationTab.this.updateLaunchConfigurationDialog();
            }
        });
        this.checkLoggingButton.setSelection(false);
    }

    /**
     * The generator seeds group, previous in analysis configuration tab, now the last section of
     * the SimuComConfigurationTab
     * 
     * @param modifyListener
     *            the ModifyListener which should be added to the elements
     */
    protected void createGeneratorSeedsGroup(final ModifyListener modifyListener) {
        final Group randomNumberGeneratorParametersGroup = new Group(this.container, SWT.NONE);
        randomNumberGeneratorParametersGroup.setText("Random Number Generator Seed");
        final GridData gd_randomNumberGeneratorParametersGroup = new GridData(SWT.FILL, SWT.CENTER, true, false);
        randomNumberGeneratorParametersGroup.setLayoutData(gd_randomNumberGeneratorParametersGroup);
        final GridLayout gridLayout_3 = new GridLayout();
        gridLayout_3.numColumns = 12;
        randomNumberGeneratorParametersGroup.setLayout(gridLayout_3);

        this.fixedSeedButton = new Button(randomNumberGeneratorParametersGroup, SWT.CHECK);
        this.fixedSeedButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 12, 1));
        this.fixedSeedButton.setText("Use a fixed seed in simulation run");
        this.fixedSeedButton.setSelection(false);
        this.fixedSeedButton.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.
             * SelectionEvent)
             */
            @Override
            public void widgetSelected(final SelectionEvent e) {
                SimuComConfigurationTab.this.setDirty(true);
                SimuComConfigurationTab.this.updateLaunchConfigurationDialog();
            }
        });

        this.seedText = new Text[6];
        final Label[] seedLabel = new Label[6];
        for (int i = 0; i < 6; i++) {
            seedLabel[i] = new Label(randomNumberGeneratorParametersGroup, SWT.NONE);
            seedLabel[i].setText("Seed " + i);
            this.seedText[i] = new Text(randomNumberGeneratorParametersGroup, SWT.BORDER);
            this.seedText[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            this.seedText[i].addModifyListener(modifyListener);
            this.seedText[i].setText(i + "");
        }
    }

    /**
     * needs the value of useAutomatedBatchMeansCheckBox to be initialised.
     * 
     * @param parentSelected
     */
    private void enableBatchMeansSettings(final boolean parentSelected) {
        this.useAutomatedBatchMeansCheckBox.setEnabled(parentSelected);

        // depend on useAutomatedBatchMeans, too
        this.batchSizeLabel.setEnabled(parentSelected && !this.useAutomatedBatchMeansCheckBox.getSelection());
        this.batchSizeField.setEnabled(parentSelected && !this.useAutomatedBatchMeansCheckBox.getSelection());
        this.minNumberOfBatchesLabel.setEnabled(parentSelected && !this.useAutomatedBatchMeansCheckBox.getSelection());
        this.minNumberOfBatchesField.setEnabled(parentSelected && !this.useAutomatedBatchMeansCheckBox.getSelection());
    }

    private void showSelectModelElementDialog() {
        final ResourceSet rs = loadModelFiles();
        final ArrayList<Object> filter = new ArrayList<Object>();
        filter.add(UsageModel.class);
        filter.add(UsageScenario.class);
        final PalladioSelectEObjectDialog dialog = new PalladioSelectEObjectDialog(this.getShell(), filter, rs);
        if (dialog.open() == org.eclipse.jface.dialogs.Dialog.OK) {
            final EObject modelElement = dialog.getResult();
            if (modelElement instanceof UsageScenario) {
                final UsageScenario usageScenario = (UsageScenario) modelElement;
                this.selectedModelElementURI = EcoreUtil.getURI(modelElement);
                this.selectedModelElementName = usageScenario.getEntityName();
                updateModelElementField(usageScenario);
            } else {
                final MessageBox warningBox = new MessageBox(this.selectModelElementField.getShell(),
                        SWT.ICON_WARNING | SWT.OK);
                warningBox.setText("Warning");
                warningBox.setMessage("No response times will be available for "
                        + "the selected model element. Please select a suitable " + "model element.");
                warningBox.open();
            }
        }
    }

    private void updateModelElementField(final UsageScenario modelElement) {
        final AdapterFactoryLabelProvider labelProvider = new AdapterFactoryLabelProvider(
                new PalladioItemProviderAdapterFactory(new UsagemodelItemProviderAdapterFactory()));
        this.selectModelElementField.setText(labelProvider.getText(modelElement));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
     */
    @Override
    public String getName() {
        return "Simulation";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.
     * ILaunchConfiguration)
     */
    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        this.recorderTabGroup.initializeFrom(configuration);

        initializeSimulatorGroup(configuration);

        try {
            this.nameField.setText(configuration.getAttribute(AbstractSimulationConfig.EXPERIMENT_RUN, ""));
        } catch (final CoreException e) {
            this.nameField.setText("MyRun");
        }
        
        try {
            this.variationField.setText(configuration.getAttribute(AbstractSimulationConfig.VARIATION_ID, ""));
        } catch (final CoreException e) {
            this.variationField.setText(AbstractSimulationConfig.DEFAULT_VARIATION_NAME);
        }

        try {
            this.timeField.setText(configuration.getAttribute(AbstractSimulationConfig.SIMULATION_TIME, ""));
        } catch (final CoreException e) {
            this.timeField.setText("150000");
        }

        try {
            this.maxMeasurementsField
                    .setText(configuration.getAttribute(AbstractSimulationConfig.MAXIMUM_MEASUREMENT_COUNT, ""));
        } catch (final CoreException e) {
            this.maxMeasurementsField.setText("10000");
        }

        try {
            final String persistenceFrameworkName = configuration
                    .getAttribute(AbstractSimulationConfig.PERSISTENCE_RECORDER_NAME, "");
            final String[] items = this.persistenceCombo.getItems();
            for (int i = 0; i < items.length; i++) {
                final String str = items[i];
                if (str.equals(persistenceFrameworkName)) {
                    this.persistenceCombo.select(i);
                }
            }
        } catch (final CoreException e) {
            if (LOGGER.isEnabledFor(Level.WARN)) {
                LOGGER.warn("Could not access persistency recorder name.", e);
            }
        }

        try {
            this.checkLoggingButton
                    .setSelection(configuration.getAttribute(AbstractSimulationConfig.VERBOSE_LOGGING, false));
        } catch (final CoreException e) {
            this.checkLoggingButton.setSelection(false);
        }

        try {
            this.levelField.setText(configuration.getAttribute(SimuComConfig.CONFIDENCE_LEVEL, "95"));
        } catch (final CoreException e) {
            this.levelField.setText("" + SimuComConfig.DEFAULT_CONFIDENCE_LEVEL);
        }

        try {
            this.halfWidthField.setText(configuration.getAttribute(SimuComConfig.CONFIDENCE_HALFWIDTH, "10"));
        } catch (final CoreException e) {
            this.halfWidthField.setText("" + SimuComConfig.DEFAULT_CONFIDENCE_HALFWIDTH);
        }

        final String defaultBatchSize = "" + SimuComConfig.DEFAULT_CONFIDENCE_BATCH_SIZE;
        try {
            this.batchSizeField
                    .setText(configuration.getAttribute(SimuComConfig.CONFIDENCE_BATCH_SIZE, defaultBatchSize));
        } catch (final CoreException e) {
            this.batchSizeField.setText(defaultBatchSize);
        }

        final String defaultMinNumberOfBatches = "" + SimuComConfig.DEFAULT_CONFIDENCE_MIN_NUMBER_OF_BATCHES;
        try {
            this.minNumberOfBatchesField.setText(configuration
                    .getAttribute(SimuComConfig.CONFIDENCE_MIN_NUMBER_OF_BATCHES, defaultMinNumberOfBatches));
        } catch (final CoreException e) {
            this.minNumberOfBatchesField.setText(defaultMinNumberOfBatches);
        }

        try {
            final String usageFile = configuration.getAttribute(ConstantsContainer.USAGE_FILE, "");
            this.modelFiles.clear();
            if (!usageFile.isEmpty()) {
                this.modelFiles.add(usageFile);
            }
        } catch (final CoreException e) {
        }

        try {
            this.selectedModelElementURI = URI
                    .createURI(configuration.getAttribute(SimuComConfig.CONFIDENCE_MODELELEMENT_URI, ""));
            final UsageScenario usageScenario = getUsageScenarioFromURI(this.selectedModelElementURI);
            this.selectedModelElementName = usageScenario.getEntityName();
            updateModelElementField(usageScenario);
        } catch (final Exception e) {
            this.selectedModelElementURI = null;
            this.selectedModelElementName = "";
            this.selectModelElementField.setText("");
        }

        // decide how to enable / disable them after initialising the values.
        try {
            final boolean isAutomaticBatches = configuration
                    .getAttribute(SimuComConfig.CONFIDENCE_USE_AUTOMATIC_BATCHES, false);
            this.useAutomatedBatchMeansCheckBox.setSelection(isAutomaticBatches);

            final boolean select = configuration.getAttribute(SimuComConfig.USE_CONFIDENCE, false);
            this.useConfidenceCheckBox.setSelection(select);

            this.levelLabel.setEnabled(select);
            this.levelField.setEnabled(select);
            this.halfWidthLabel.setEnabled(select);
            this.halfWidthField.setEnabled(select);
            this.selectModelElementLabel.setEnabled(select);
            this.selectModelElementField.setEnabled(select);
            this.selectModelElementButton.setEnabled(select);

            // needs the value of useAutomatedBatchMeansCheckBox to be initialised.
            enableBatchMeansSettings(select);

        } catch (final CoreException e) {
            this.useConfidenceCheckBox.setSelection(false);
            this.levelLabel.setEnabled(false);
            this.levelField.setEnabled(false);
            this.halfWidthLabel.setEnabled(false);
            this.halfWidthField.setEnabled(false);
            this.selectModelElementLabel.setEnabled(false);
            this.selectModelElementField.setEnabled(false);
            this.selectModelElementButton.setEnabled(false);
            enableBatchMeansSettings(false);

        }
        try {
            this.fixedSeedButton
                    .setSelection(configuration.getAttribute(AbstractSimulationConfig.USE_FIXED_SEED, false));
        } catch (final CoreException e) {
            this.fixedSeedButton.setSelection(false);
        }

        for (int i = 0; i < 6; i++) {
            try {
                this.seedText[i]
                        .setText(configuration.getAttribute(AbstractSimulationConfig.FIXED_SEED_PREFIX + i, i + ""));
            } catch (final CoreException e) {
                this.seedText[i].setText(i + "");
            }
        }
    }

    /**
     * initializes the simulator group with the help of the given configuration
     * 
     * @param configuration
     *            the given ILaunchConfiguration
     */
    protected void initializeSimulatorGroup(final ILaunchConfiguration configuration) {
        try {
            final String simulatorId = configuration.getAttribute(AbstractSimulationConfig.SIMULATOR_ID,
                    AbstractSimulationConfig.DEFAULT_SIMULATOR_ID);
            final String simulatorName = SimulatorExtensionHelper.getSimulatorNameForId(simulatorId);
            final String[] items = this.simulatorCombo.getItems();
            for (int i = 0; i < items.length; i++) {
                final String currentItemName = items[i];
                if (currentItemName.equals(simulatorName)) {
                    this.simulatorCombo.select(i);
                }
            }
        } catch (final CoreException e) {
            if (LOGGER.isEnabledFor(Level.WARN)) {
                LOGGER.warn("Could not initialise simulator selection.", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.
     * ILaunchConfigurationWorkingCopy)
     */
    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        // delegate this method call to recorder tab group
        this.recorderTabGroup.performApply(configuration);

        // apply simulator selection
        applySimulatorGroup(configuration);

        configuration.setAttribute(AbstractSimulationConfig.EXPERIMENT_RUN, this.nameField.getText());
        configuration.setAttribute(AbstractSimulationConfig.VARIATION_ID, this.variationField.getText());

        configuration.setAttribute(AbstractSimulationConfig.SIMULATION_TIME, this.timeField.getText());
        configuration.setAttribute(AbstractSimulationConfig.MAXIMUM_MEASUREMENT_COUNT,
                this.maxMeasurementsField.getText());
        configuration.setAttribute(AbstractSimulationConfig.PERSISTENCE_RECORDER_NAME, this.persistenceCombo.getText());
        configuration.setAttribute(AbstractSimulationConfig.VERBOSE_LOGGING, this.checkLoggingButton.getSelection());
        configuration.setAttribute(SimuComConfig.USE_CONFIDENCE, this.useConfidenceCheckBox.getSelection());
        configuration.setAttribute(SimuComConfig.CONFIDENCE_LEVEL, this.levelField.getText());
        configuration.setAttribute(SimuComConfig.CONFIDENCE_HALFWIDTH, this.halfWidthField.getText());
        configuration.setAttribute(SimuComConfig.CONFIDENCE_MODELELEMENT_NAME, this.selectedModelElementName);
        configuration.setAttribute(AbstractSimulationConfig.USE_FIXED_SEED, this.fixedSeedButton.getSelection());
        for (int i = 0; i < 6; i++) {
            configuration.setAttribute(AbstractSimulationConfig.FIXED_SEED_PREFIX + i, this.seedText[i].getText());
        }

        if (this.selectedModelElementURI != null) {
            configuration.setAttribute(SimuComConfig.CONFIDENCE_MODELELEMENT_URI,
                    this.selectedModelElementURI.toString());
        } else {
            configuration.setAttribute(SimuComConfig.CONFIDENCE_MODELELEMENT_URI, "");
        }
        configuration.setAttribute(SimuComConfig.CONFIDENCE_USE_AUTOMATIC_BATCHES,
                this.useAutomatedBatchMeansCheckBox.getSelection());
        configuration.setAttribute(SimuComConfig.CONFIDENCE_BATCH_SIZE, this.batchSizeField.getText());
        configuration.setAttribute(SimuComConfig.CONFIDENCE_MIN_NUMBER_OF_BATCHES,
                this.minNumberOfBatchesField.getText());
    }

    /**
     * Applies the configuration for the simulator group
     * 
     * @param configuration
     *            the configuration where the values should be written to
     */
    protected void applySimulatorGroup(final ILaunchConfigurationWorkingCopy configuration) {
        try {
            // find simulator id for the given simulator name
            final String simulatorId = SimulatorExtensionHelper.getSimulatorIdForName(this.simulatorCombo.getText());
            configuration.setAttribute(AbstractSimulationConfig.SIMULATOR_ID, simulatorId);
        } catch (final CoreException e) {
            if (LOGGER.isEnabledFor(Level.ERROR)) {
                LOGGER.error("Failed to retrieve the id for simulator \"" + this.simulatorCombo.getText() + "\"");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.
     * ILaunchConfigurationWorkingCopy)
     */
    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        // delegate this method call to recorder tab group
        if (this.recorderTabGroup != null) {
            this.recorderTabGroup.setDefaults(configuration);
        }

        configuration.setAttribute(AbstractSimulationConfig.SIMULATOR_ID,
                AbstractSimulationConfig.DEFAULT_SIMULATOR_ID);
        configuration.setAttribute(AbstractSimulationConfig.EXPERIMENT_RUN,
                AbstractSimulationConfig.DEFAULT_EXPERIMENT_RUN);
        configuration.setAttribute(AbstractSimulationConfig.VARIATION_ID,
                AbstractSimulationConfig.DEFAULT_VARIATION_NAME);
        configuration.setAttribute(AbstractSimulationConfig.SIMULATION_TIME,
                AbstractSimulationConfig.DEFAULT_SIMULATION_TIME);
        configuration.setAttribute(AbstractSimulationConfig.MAXIMUM_MEASUREMENT_COUNT,
                AbstractSimulationConfig.DEFAULT_MAXIMUM_MEASUREMENT_COUNT);
        configuration.setAttribute(AbstractSimulationConfig.PERSISTENCE_RECORDER_NAME,
                AbstractSimulationConfig.DEFAULT_PERSISTENCE_RECORDER_NAME);
        configuration.setAttribute(SimuComConfig.USE_CONFIDENCE, SimuComConfig.DEFAULT_USE_CONFIDENCE);
        configuration.setAttribute(SimuComConfig.CONFIDENCE_LEVEL, SimuComConfig.DEFAULT_CONFIDENCE_LEVEL);
        configuration.setAttribute(SimuComConfig.CONFIDENCE_HALFWIDTH, SimuComConfig.DEFAULT_CONFIDENCE_HALFWIDTH);
        configuration.setAttribute(SimuComConfig.CONFIDENCE_MODELELEMENT_NAME,
                SimuComConfig.DEFAULT_CONFIDENCE_MODELELEMENT_NAME);
        configuration.setAttribute(SimuComConfig.CONFIDENCE_MODELELEMENT_URI,
                SimuComConfig.DEFAULT_CONFIDENCE_MODELELEMENT_URI);
        configuration.setAttribute(SimuComConfig.CONFIDENCE_USE_AUTOMATIC_BATCHES,
                SimuComConfig.DEFAULT_CONFIDENCE_USE_AUTOMATIC_BATCHES);
        configuration.setAttribute(SimuComConfig.CONFIDENCE_BATCH_SIZE, SimuComConfig.DEFAULT_CONFIDENCE_BATCH_SIZE);
        configuration.setAttribute(SimuComConfig.CONFIDENCE_MIN_NUMBER_OF_BATCHES,
                SimuComConfig.DEFAULT_CONFIDENCE_MIN_NUMBER_OF_BATCHES);

        // set default value for persistence framework
        final List<String> recorderNames = RecorderExtensionHelper.getRecorderNames();
        if (recorderNames.size() > 0) {
            configuration.setAttribute(AbstractSimulationConfig.PERSISTENCE_RECORDER_NAME, recorderNames.get(0));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
     */
    @Override
    public Image getImage() {
        return SimuControllerImages.imageRegistry.get(SimuControllerImages.SIMUCOM_CONF);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.
     * ILaunchConfiguration)
     */
    @Override
    public boolean isValid(final ILaunchConfiguration launchConfig) {
        setErrorMessage(null);

        // Validation of simulator group
        if (!isSimulatorGroupValid()) {
            return false;
        }
        if (this.nameField.getText().equals("")) {
            setErrorMessage("ExperimentRun name is missing!");
            return false;
        }
        /* No requirements for variationField*/
        if (this.timeField.getText().equals("")) {
            setErrorMessage("Simulation time is missing!");
            return false;
        }
        if (this.maxMeasurementsField.getText().equals("")) {
            setErrorMessage("Maximum Measurement counter is missing!");
            return false;
        }
        final String persistenceFrameworkName = this.persistenceCombo.getText();
        if (persistenceFrameworkName == null || persistenceFrameworkName.isEmpty()) {
            setErrorMessage("Persistence Framework is missing!");
            return false;
        }
        // delegate validation to persistence framework tabs
        final ILaunchConfigurationTab[] recorderTabs = this.recorderTabGroup.getTabs();
        for (final ILaunchConfigurationTab tab : recorderTabs) {
            if (persistenceFrameworkName.contains(tab.getName()) && !tab.isValid(launchConfig)) {
                setErrorMessage(persistenceFrameworkName + ": " + tab.getErrorMessage());
                return false;
            }
        }
        // check confidence level
        if (this.useConfidenceCheckBox.getSelection() && "".equals(this.levelField.getText())) {
            setErrorMessage("Confidence level is missing!");
            return false;
        } else if (this.useConfidenceCheckBox.getSelection() && "".equals(this.halfWidthField.getText())) {
            setErrorMessage("Confidence interval half-width is missing!");
            return false;
        } else if (this.useConfidenceCheckBox.getSelection() && "".equals(this.selectModelElementField.getText())) {
            setErrorMessage("Specify the usage scenario for which the confidence interval should be determined.");
            return false;
        } else if (this.useConfidenceCheckBox.getSelection() && !"".equals(this.levelField.getText())) {
            try {
                final double level = Double.parseDouble(this.levelField.getText());
                if (level < 0 || level > 100) {
                    setErrorMessage("Confidence level has to be a percentage!");
                    return false;
                }
            } catch (final NumberFormatException ex) {
                setErrorMessage("Confidence level has to be an number!");
                return false;
            }
        }
        // check confidence interval half-width
        if (this.useConfidenceCheckBox.getSelection() && this.halfWidthField.getText().equals("")) {
            setErrorMessage("Confidence interval half-width is missing!");
            return false;
        } else if (this.useConfidenceCheckBox.getSelection() && this.halfWidthField.getText().length() > 0) {
            try {
                final int halfWidth = Integer.parseInt(this.halfWidthField.getText());
                if (halfWidth < 0 || halfWidth > 100) {
                    setErrorMessage("Half-width has to be a percentage!");
                    return false;
                }
            } catch (final NumberFormatException ex) {
                setErrorMessage("Half-width has to be an integer!");
                return false;
            }
        }
        // check, whether a model element is selected
        if (this.useConfidenceCheckBox.getSelection() && this.selectedModelElementURI == null) {
            setErrorMessage("Select a model element whose response times are " + "to be monitored!");
        }

        // check validity of batch size and number settings
        if (this.useConfidenceCheckBox.getSelection() && !this.useAutomatedBatchMeansCheckBox.getSelection()) {
            if ("".equals(this.batchSizeField.getText())) {
                setErrorMessage("Batch size has to be specified if not determined automatically.");
                return false;
            } else if ("".equals(this.minNumberOfBatchesField.getText())) {
                setErrorMessage("Minimum number of batches have to be specified if not determined automatically.");
                return false;
            }

            try {
                Integer.parseInt(this.batchSizeField.getText());
            } catch (final NumberFormatException ex) {
                setErrorMessage("Batch size has to be an integer!");
                return false;
            }

            try {
                Integer.parseInt(this.minNumberOfBatchesField.getText());
            } catch (final NumberFormatException ex) {
                setErrorMessage("Minimum number of batches has to be an integer!");
                return false;
            }

        }

        return true;
    }

    /**
     * Validation of the simulator group
     */
    protected boolean isSimulatorGroupValid() {
        final String simulatorName = this.simulatorCombo.getText();
        if (simulatorName == null || simulatorName.isEmpty()) {
            setErrorMessage("Simulator implementation is missing!");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void activated(final ILaunchConfigurationWorkingCopy configuration) {
        try {
            final String usageFile = configuration.getAttribute(ConstantsContainer.USAGE_FILE, "");

            if (!usageFile.isEmpty()) {
                // Prevent reloading the model elements when usage file has not
                // changed
                if (!this.modelFiles.contains(usageFile)) {
                    this.modelFiles.clear();
                    this.modelFiles.add(usageFile);

                    this.selectedModelElementURI = URI
                            .createURI(configuration.getAttribute(SimuComConfig.CONFIDENCE_MODELELEMENT_URI, ""));
                    final UsageScenario usageScenario = getUsageScenarioFromURI(this.selectedModelElementURI);
                    this.selectedModelElementName = usageScenario.getEntityName();
                    updateModelElementField(usageScenario);
                }
            }
        } catch (final Exception e) {
            this.selectedModelElementURI = null;
            this.selectedModelElementName = "";
            this.selectModelElementField.setText("");
        }

    }

    @Override
    public void deactivated(final ILaunchConfigurationWorkingCopy workingCopy) {
    }

    private ResourceSet loadModelFiles() {
        final ResourceSet rs = new ResourceSetImpl();
        for (final String file : this.modelFiles) {
            try {
                rs.getResource(URI.createURI(file), true);
            } catch (final Exception ex) {
                try {
                    rs.getResource(URI.createFileURI(file), true);
                } catch (final Exception exInner) {
                }
            }
        }
        EcoreUtil.resolveAll(rs);
        return rs;
    }

    private UsageScenario getUsageScenarioFromURI(final URI selectedModelElementURI) throws Exception {
        final ResourceSet rs = loadModelFiles();
        final EObject selectedModelElement = rs.getEObject(selectedModelElementURI, false);

        if (selectedModelElement != null && selectedModelElement instanceof UsageScenario) {
            return (UsageScenario) selectedModelElement;
        } else {
            throw new RuntimeException("selectedModelElement " + "is null or of wrong type");
        }
    }

}
