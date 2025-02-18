package de.uka.ipd.sdq.codegen.simucontroller.workflow.jobs;

import java.io.PrintStream;

import org.palladiosimulator.analyzer.workflow.core.configurations.AbstractCodeGenerationWorkflowRunConfiguration;

import de.uka.ipd.sdq.workflow.jobs.IJob;

public class CreateSimuComMetaDataFilesJob extends AbstractCreateMetaDataFilesJob implements IJob {

    public CreateSimuComMetaDataFilesJob(final AbstractCodeGenerationWorkflowRunConfiguration configuration) {
        super();

        this.configuration = configuration;
    }

    @Override
    protected void writePluginXmlContent(final PrintStream out) {
        out.println("<?xml version='1.0'?>"); //$NON-NLS-1$
        out.println("<plugin>"); //$NON-NLS-1$
        out.println("   <extension"); //$NON-NLS-1$
        out.println("         point=\"de.uka.ipd.sdq.simucomframework.controller\">"); //$NON-NLS-1$
        out.println("      <actionDelegate"); //$NON-NLS-1$
        out.println("            class=\"main.SimuComControl\""); //$NON-NLS-1$
        out.println("            id=\"de.uka.ipd.sdq.codegen.simucominstance.actionDelegate\">"); //$NON-NLS-1$
        out.println("      </actionDelegate>"); //$NON-NLS-1$
        out.println("   </extension>"); //$NON-NLS-1$
        out.println("</plugin>"); //$NON-NLS-1$
    }

    @Override
    protected void writeBuildPropertiesContent(final PrintStream out) {
        out.println("output.. = bin/"); //$NON-NLS-1$
        out.println("source.. = src/"); //$NON-NLS-1$
        out.println("bin.includes = plugin.xml,\\"); //$NON-NLS-1$
        out.println("				META-INF/,\\"); //$NON-NLS-1$
        out.println("				."); //$NON-NLS-1$
    }

    private final static String[] BUNDLES = new String[] {
            "de.uka.ipd.sdq.simulation", "de.uka.ipd.sdq.simulation.abstractsimengine",
            "de.uka.ipd.sdq.simucomframework", "de.uka.ipd.sdq.simucomframework.simucomstatus",
            "de.uka.ipd.sdq.simucomframework.variables", "org.apache.log4j", "org.eclipse.osgi",
            "de.uka.ipd.sdq.scheduler", "org.jscience", "org.palladiosimulator.probeframework",
            "org.palladiosimulator.metricspec", "org.palladiosimulator.reliability", "org.palladiosimulator.analyzer.accuracy",
            "de.uka.ipd.sdq.probfunction.math", "org.palladiosimulator.measurementframework;bundle-version=\"1.0.0\"",
            "org.palladiosimulator.edp2;bundle-version=\"2.0.0\""
    };

    @Override
    protected String[] getRequiredBundles() {
        return BUNDLES;
    }

    @Override
    protected String getBundleActivator() {
        return "main.SimuComControl";
    }

}
