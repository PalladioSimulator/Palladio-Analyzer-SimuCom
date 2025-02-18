package de.uka.ipd.sdq.codegen.simucontroller.workflow.jobs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.palladiosimulator.analyzer.workflow.core.configurations.AbstractCodeGenerationWorkflowRunConfiguration;
import org.palladiosimulator.analyzer.workflow.jobs.CreatePluginProjectJob;

import de.uka.ipd.sdq.workflow.jobs.CleanupFailedException;
import de.uka.ipd.sdq.workflow.jobs.JobFailedException;
import de.uka.ipd.sdq.workflow.jobs.UserCanceledException;

public abstract class AbstractCreateMetaDataFilesJob {

    protected AbstractCodeGenerationWorkflowRunConfiguration configuration;
    public static final String F_MANIFEST = "MANIFEST.MF";
    public static final String F_MANIFEST_FP = "META-INF/" + F_MANIFEST;
    public static final String F_PLUGIN = "plugin.xml";
    public static final String F_FRAGMENT = "fragment.xml";
    public static final String F_PROPERTIES = ".properties";
    public static final String F_BUILD = "build" + F_PROPERTIES;
    public static final String F_CONTENT = "content" + F_PROPERTIES;

    public AbstractCreateMetaDataFilesJob() {
        super();
    }

    public void execute(final IProgressMonitor monitor) throws JobFailedException, UserCanceledException {
        final IProject project = CreatePluginProjectJob.getProject(configuration.getStoragePluginID());

        try {
            createPluginXml(project);
            createManifestMf(project);
            createBuildProperties(project);
            createAdditionalProperties(project);
        } catch (final CoreException e) {
            throw new JobFailedException("Failed to create plugin metadata files", e);
        }
    }

    public String getName() {
        return "Create SimuCom Metadata Files";
    }

    public void cleanup(final IProgressMonitor monitor) throws CleanupFailedException {
        // Nothing to do
    }

    private void createPluginXml(final IProject project) throws CoreException {

        ByteArrayOutputStream baos;
        PrintStream out;

        baos = new ByteArrayOutputStream();
        out = new PrintStream(baos);

        writePluginXmlContent(out);

        out.close();

        final IFile pluginXml = project.getFile(F_PLUGIN);
        if (!pluginXml.exists()) {
            pluginXml.create(new ByteArrayInputStream(baos.toByteArray()), true, null);
        }
    }

    protected abstract void writePluginXmlContent(PrintStream out);

    private void createBuildProperties(final IProject project) throws CoreException {

        ByteArrayOutputStream baos;
        PrintStream out;

        baos = new ByteArrayOutputStream();
        out = new PrintStream(baos);

        writeBuildPropertiesContent(out);

        out.close();

        final IFile buildProperties = project.getFile(F_BUILD);
        if (!buildProperties.exists()) {
            buildProperties.create(new ByteArrayInputStream(baos.toByteArray()), true, null);
        }
    }

    protected abstract void writeBuildPropertiesContent(PrintStream out);

    private void createManifestMf(final IProject project) throws CoreException {

        ByteArrayOutputStream baos;
        PrintStream out;

        baos = new ByteArrayOutputStream();
        out = new PrintStream(baos);

        out.println("Manifest-Version: 1.0"); //$NON-NLS-1$
        out.println("Bundle-ManifestVersion: 2"); //$NON-NLS-1$
        out.println("Bundle-Name: SimuCom Instance Plug-in"); //$NON-NLS-1$
        out.println("Bundle-SymbolicName: " + project.getName() + ";singleton:=true"); //$NON-NLS-1$
        out.println("Bundle-Version: 1.0.0"); //$NON-NLS-1$
        out.println("Bundle-Activator: " + getBundleActivator());

        out.print("Require-Bundle: ");

        final List<String> requiredBundles = new ArrayList<String>();
        requiredBundles.addAll(Arrays.asList(getRequiredBundles()));
        requiredBundles.addAll(configuration.getCodeGenerationRequiredBundles());

        out.println(StringUtils.join(requiredBundles, ",\n "));

        out.println("Bundle-ActivationPolicy: lazy"); //$NON-NLS-1$
        out.println("Bundle-ClassPath: bin/,");
        out.println(" .");
        out.println("Bundle-RequiredExecutionEnvironment: JavaSE-1.7");
        // out.println("Export-Package: main");

        out.close();

        final IFile manifestMf = project.getFile(F_MANIFEST_FP);
        if (!manifestMf.exists()) {
            manifestMf.create(new ByteArrayInputStream(baos.toByteArray()), true, null);
        }
    }

    protected abstract String[] getRequiredBundles();

    protected abstract String getBundleActivator();

    /*
     * Creates the content.properties file of the generated project which is necessary for rerunning
     * the simulation
     */
    private void createAdditionalProperties(final IProject project) throws CoreException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream out = new PrintStream(baos);

        out.println("#####Contains data about the original models on which this project was built#####");
        if (configuration.getStoragePluginID() != null) {
            out.println("baseProjectID = " + configuration.getBaseProjectID());

            for (final String path : configuration.getModelPaths()) {
                final String fileEnding = path.substring(path.lastIndexOf(".") + 1);
                out.println(fileEnding + " = " + path);
            }
        }

        final IFile contentProp = project.getFile(F_CONTENT);
        if (!contentProp.exists()) {
            contentProp.create(new ByteArrayInputStream(baos.toByteArray()), true, null);
        }
    }

}