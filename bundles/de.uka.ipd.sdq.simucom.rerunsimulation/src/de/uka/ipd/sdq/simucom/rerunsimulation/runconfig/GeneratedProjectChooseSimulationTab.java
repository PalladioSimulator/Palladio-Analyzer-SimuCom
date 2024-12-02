package de.uka.ipd.sdq.simucom.rerunsimulation.runconfig;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.palladiosimulator.analyzer.workflow.ConstantsContainer;

import de.uka.ipd.sdq.simucom.rerunsimulation.filter.ProjectFilterPattern;
import de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper;

/**
 * The tab in the rerun simulation configuration that shows the generated project and its models
 * used. Currently, all model paths have to be included in this tab since
 * de.uka.ipd.sdq.simucom.rerunsimulation.jobs that are needed in order for the rerun simulator to
 * work, include checking the validity of the models.
 * 
 * @author Michael Junker
 *
 */
public class GeneratedProjectChooseSimulationTab extends AbstractLaunchConfigurationTab {

    private Composite container;

    private Text txtGeneratedProject;
    private Text txtBelongsToProject;
    private Text txtAllocationFile;
    private Text txtUsageModelFile;
    private Text txtEventMiddlewareFile;
    private Text txtRepositoryMiddlewareFile;
    private Text txtFeatureConfigFile;
    private Button fileSystemChooser;
    private Button bUsageCheck;
    private Button bAllocationCheck;
    private Button bSystemCheck;

    @Override
    public void createControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout());
        setControl(container);

        ModifyListener modify = new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                setDirty(true);
                updateLaunchConfigurationDialog();
            }

        };
        
        MouseListener mouse = new MouseAdapter() {
           
            @Override
            public void mouseUp(MouseEvent e) {
                setDirty(true);
                updateLaunchConfigurationDialog();
            } 
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                setDirty(true);
                updateLaunchConfigurationDialog();
            } 
        };
        
        final Group generatedProjectInputGroup = new Group(container, SWT.BORDER);
        final GridLayout glGeneratedProjectInputGroup = new GridLayout();
        glGeneratedProjectInputGroup.numColumns = 2;
        glGeneratedProjectInputGroup.makeColumnsEqualWidth = false;

        generatedProjectInputGroup.setLayout(glGeneratedProjectInputGroup);
        generatedProjectInputGroup.setText("Choose existing generated project");
        generatedProjectInputGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        txtGeneratedProject = new Text(generatedProjectInputGroup, SWT.SINGLE | SWT.BORDER);
        final GridData gridDataTextFileName = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridDataTextFileName.widthHint = 200;
        txtGeneratedProject.setLayoutData(gridDataTextFileName);
        txtGeneratedProject.addModifyListener(modify);

        fileSystemChooser = new Button(generatedProjectInputGroup, SWT.NONE);
        fileSystemChooser.setText("Workspace...");
        fileSystemChooser.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                List<ViewerFilter> filters = new ArrayList<ViewerFilter>();
                ProjectFilterPattern filter = new ProjectFilterPattern();
                filter.setPattern("/*");
                filters.add(filter);

                IContainer[] container = WorkspaceResourceDialog.openFolderSelection(getShell(),
                        "Select project to run again", "Select project to run again", false, null, filters);

                txtGeneratedProject.setText(container[0].getFullPath().toPortableString());
                readPropertiesFile(container[0].getFullPath().toPortableString());
            }
        });

        final Group gBelongsToProject = new Group(container, SWT.BORDER);
        final GridLayout glBelongsToProject = new GridLayout();
        GridData gDBelongsToProject = new GridData(SWT.FILL, SWT.CENTER, true, false);

        glBelongsToProject.numColumns = 2;
        glBelongsToProject.makeColumnsEqualWidth = false;

        gBelongsToProject.setLayout(glBelongsToProject);
        gBelongsToProject.setText("Base project information");
        gBelongsToProject.setLayoutData(gDBelongsToProject);

        Label lBelongsToProject = new Label(gBelongsToProject, SWT.NONE);
        lBelongsToProject.setText("Base Project");

        txtBelongsToProject = new Text(gBelongsToProject, SWT.SINGLE | SWT.BORDER);
        txtBelongsToProject.setLayoutData(gDBelongsToProject);
        txtBelongsToProject.setVisible(true);
        txtBelongsToProject.setEnabled(false);

        final Group gAllocation = new Group(container, SWT.BORDER);
        final GridLayout glAllocation = new GridLayout();
        GridData gDAllocation = new GridData(SWT.FILL, SWT.CENTER, true, false);
        txtAllocationFile = new Text(gAllocation, SWT.SINGLE | SWT.BORDER);
        TabHelper.createFileInputSection(gAllocation, modify, "Allocation File",
                ConstantsContainer.ALLOCATION_EXTENSION, txtAllocationFile, "Select Allocation File", getShell(),
                ConstantsContainer.DEFAULT_ALLOCATION_FILE);
        glAllocation.numColumns = 5;
        glAllocation.makeColumnsEqualWidth = false;
        gAllocation.setLayout(glAllocation);
        gAllocation.setLayoutData(gDAllocation);
        Group gCheckButtons = new Group(gAllocation, SWT.NONE);
        gCheckButtons.setLayout(new GridLayout());
        bAllocationCheck = new Button(gCheckButtons, SWT.CHECK);
        bAllocationCheck.setText("Regenerate allocation");
        bAllocationCheck.addMouseListener(mouse);
        bSystemCheck = new Button(gCheckButtons, SWT.CHECK);
        bSystemCheck.setText("Regenerate system and allocation");
        bSystemCheck.addMouseListener(mouse);
        
        final Group gUsage = new Group(container, SWT.BORDER);
        final GridLayout glUsage = new GridLayout();
        GridData gDUsage = new GridData(SWT.FILL, SWT.CENTER, true, false);
        txtUsageModelFile = new Text(gUsage, SWT.SINGLE | SWT.BORDER);
        TabHelper.createFileInputSection(gUsage, modify, "Usage File", ConstantsContainer.USAGEMODEL_EXTENSION,
                txtUsageModelFile, "Select Usage File", getShell(), ConstantsContainer.DEFAULT_USAGE_FILE);
        glUsage.numColumns = 5;
        glUsage.makeColumnsEqualWidth = false;
        gUsage.setLayout(glUsage);
        gUsage.setLayoutData(gDUsage);
        bUsageCheck = new Button(gUsage, SWT.CHECK);
        bUsageCheck.setText("Regenerate usage model");
        bUsageCheck.addMouseListener(mouse);

        final Group gModelFiles = new Group(container, SWT.BORDER);
        final GridLayout glModelFiles = new GridLayout();
        GridData gDModelFiles = new GridData(SWT.FILL, SWT.CENTER, true, false);

        glModelFiles.numColumns = 2;
        glModelFiles.makeColumnsEqualWidth = false;

        gModelFiles.setLayout(glModelFiles);
        gModelFiles.setText("Model Files");
        gModelFiles.setLayoutData(gDModelFiles);
        gModelFiles.setVisible(false);

        Label lEventMiddlewareFile = new Label(gModelFiles, SWT.NONE);
        lEventMiddlewareFile.setText("Event Middleware File");
        txtEventMiddlewareFile = new Text(gModelFiles, SWT.SINGLE | SWT.BORDER);
        txtEventMiddlewareFile.setLayoutData(gridDataTextFileName);
        // Should be set to true if not the default model is used
        lEventMiddlewareFile.setVisible(true);
        txtEventMiddlewareFile.setVisible(true);
        txtEventMiddlewareFile.setEnabled(false);

        Label lRepositoryMiddlewareFile = new Label(gModelFiles, SWT.NONE);
        lRepositoryMiddlewareFile.setText("Repository Middleware File");
        txtRepositoryMiddlewareFile = new Text(gModelFiles, SWT.SINGLE | SWT.BORDER);
        txtRepositoryMiddlewareFile.setLayoutData(gridDataTextFileName);
        // Should be set to true if not the default model is used
        lRepositoryMiddlewareFile.setVisible(true);
        txtRepositoryMiddlewareFile.setVisible(true);
        txtRepositoryMiddlewareFile.setEnabled(false);

        Label lFeatureConfigFile = new Label(gModelFiles, SWT.NONE);
        lFeatureConfigFile.setText("Feature config file");
        txtFeatureConfigFile = new Text(gModelFiles, SWT.SINGLE | SWT.BORDER);
        txtFeatureConfigFile.setLayoutData(gridDataTextFileName);
        // Should be set to true if not the default model is used
        lFeatureConfigFile.setVisible(true);
        txtFeatureConfigFile.setVisible(true);
        txtFeatureConfigFile.setEnabled(false);
    }

    /*
     * Reads the contents.properties file in the generated project
     */
    private void readPropertiesFile(String projectPath) {
        Properties prop = new Properties();
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectPath);
        IResource resource = project.findMember("/content.properties");
        try {
            // URL url = resource.getLocationURI().toURL();
            URL url = resource.getLocationURI().toURL();
            prop.load(url.openStream());

            txtBelongsToProject.setText(prop.getProperty(ConstantsContainer.BASE_PROJECT_ID));
            txtAllocationFile.setText(prop.getProperty("allocation"));
            txtUsageModelFile.setText(prop.getProperty("usagemodel"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(ConstantsContainer.BASE_PROJECT_ID, "");
        configuration.setAttribute(ConstantsContainer.TEMPORARY_DATA_LOCATION,
                ConstantsContainer.DEFAULT_TEMPORARY_DATA_LOCATION);
        configuration.setAttribute(ConstantsContainer.ALLOCATION_FILE, "");
        configuration.setAttribute(ConstantsContainer.USAGE_FILE, "");
        configuration.setAttribute(ConstantsContainer.EVENT_MIDDLEWARE_REPOSITORY_FILE,
                ConstantsContainer.DEFAULT_EVENT_MIDDLEWARE_FILE);
        configuration.setAttribute(ConstantsContainer.RMI_MIDDLEWARE_REPOSITORY_FILE,
                ConstantsContainer.DEFAULT_RMI_MIDDLEWARE_REPOSITORY_FILE);
        configuration.setAttribute(ConstantsContainer.FEATURE_CONFIG,
                ConstantsContainer.DEFAULT_FEATURE_CONFIGURATION_FILE);
        configuration.setAttribute(ConstantsContainer.DELETE_TEMPORARY_DATA_AFTER_ANALYSIS, false);
        configuration.setAttribute(ConstantsContainer.ANALYSE_ACCURACY, false);
        configuration.setAttribute(ConstantsContainer.ACCURACY_QUALITY_ANNOTATION_FILE,
                ConstantsContainer.DEFAULT_ACCURACY_QUALITY_ANNOTATION_FILE);
        configuration.setAttribute(ConstantsContainer.REGENERATE_ALLOCATION, false);
        configuration.setAttribute(ConstantsContainer.REGENERATE_USAGE, false);
        configuration.setAttribute(ConstantsContainer.REGENERATE_SYSTEM, false);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            txtGeneratedProject.setText(configuration.getAttribute(ConstantsContainer.GENERATED_PROJECT_PATH, ""));
        } catch (CoreException e1) {
            e1.printStackTrace();
        }
        try {
            txtBelongsToProject.setText(configuration.getAttribute(ConstantsContainer.BASE_PROJECT_ID, ""));
        } catch (CoreException e) {
            e.printStackTrace();
        }
        try {
            txtAllocationFile.setText(configuration.getAttribute(ConstantsContainer.ALLOCATION_FILE, ""));
        } catch (CoreException e) {
            e.printStackTrace();
        }
        try {
            bAllocationCheck.setSelection(configuration.getAttribute(ConstantsContainer.REGENERATE_ALLOCATION, false));
        } catch (CoreException e) {
            e.printStackTrace();
        }
        try {
            bSystemCheck.setSelection(configuration.getAttribute(ConstantsContainer.REGENERATE_SYSTEM, false));
        } catch (CoreException e) {
            e.printStackTrace();
        }
        try {
            txtUsageModelFile.setText(configuration.getAttribute(ConstantsContainer.USAGE_FILE, ""));
        } catch (CoreException e) {
            e.printStackTrace();
        }
        try {
            bUsageCheck.setSelection(configuration.getAttribute(ConstantsContainer.REGENERATE_USAGE, false));
        } catch (CoreException e) {
            e.printStackTrace();
        }
        try {
            txtRepositoryMiddlewareFile.setText(configuration.getAttribute(
                    ConstantsContainer.RMI_MIDDLEWARE_REPOSITORY_FILE,
                    ConstantsContainer.DEFAULT_RMI_MIDDLEWARE_REPOSITORY_FILE));
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            txtEventMiddlewareFile.setText(configuration.getAttribute(
                    ConstantsContainer.EVENT_MIDDLEWARE_REPOSITORY_FILE,
                    ConstantsContainer.DEFAULT_EVENT_MIDDLEWARE_FILE));
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            txtFeatureConfigFile.setText(configuration.getAttribute(ConstantsContainer.FEATURE_CONFIG,
                    ConstantsContainer.DEFAULT_FEATURE_CONFIGURATION_FILE));
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {

        configuration.setAttribute(ConstantsContainer.BASE_PROJECT_ID, txtBelongsToProject.getText());
        configuration.setAttribute(ConstantsContainer.TEMPORARY_DATA_LOCATION, txtGeneratedProject.getText());
        configuration.setAttribute(ConstantsContainer.GENERATED_PROJECT_PATH, txtGeneratedProject.getText());
        configuration.setAttribute(ConstantsContainer.ALLOCATION_FILE, txtAllocationFile.getText());
        configuration.setAttribute(ConstantsContainer.USAGE_FILE, txtUsageModelFile.getText());
        configuration.setAttribute(ConstantsContainer.DELETE_TEMPORARY_DATA_AFTER_ANALYSIS, false);
        configuration.setAttribute(ConstantsContainer.ANALYSE_ACCURACY, false);
        configuration.setAttribute(ConstantsContainer.ACCURACY_QUALITY_ANNOTATION_FILE,
                ConstantsContainer.DEFAULT_ACCURACY_QUALITY_ANNOTATION_FILE);

        configuration.setAttribute(ConstantsContainer.EVENT_MIDDLEWARE_REPOSITORY_FILE,
                ConstantsContainer.DEFAULT_EVENT_MIDDLEWARE_FILE);
        configuration.setAttribute(ConstantsContainer.RMI_MIDDLEWARE_REPOSITORY_FILE,
                ConstantsContainer.DEFAULT_RMI_MIDDLEWARE_REPOSITORY_FILE);
        configuration.setAttribute(ConstantsContainer.FEATURE_CONFIG,
                ConstantsContainer.DEFAULT_FEATURE_CONFIGURATION_FILE);
        configuration.setAttribute(ConstantsContainer.REGENERATE_ALLOCATION, bAllocationCheck.getSelection());
        configuration.setAttribute(ConstantsContainer.REGENERATE_USAGE, bUsageCheck.getSelection());
        configuration.setAttribute(ConstantsContainer.REGENERATE_SYSTEM, bSystemCheck.getSelection());
    }

    @Override
    public String getName() {
        return "Rerun Configuration";
    }

}
