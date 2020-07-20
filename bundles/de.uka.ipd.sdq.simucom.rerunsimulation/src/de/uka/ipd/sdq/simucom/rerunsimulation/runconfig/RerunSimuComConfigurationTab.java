package de.uka.ipd.sdq.simucom.rerunsimulation.runconfig;

import org.eclipse.debug.ui.ILaunchConfigurationTab;

import de.uka.ipd.sdq.codegen.simucontroller.runconfig.SimuComConfigurationTab;

/**
 * This tab is basically the same tab as SimuComConfigurationTab. The only difference is that in
 * this tab the first section, the simulator section, is left out since there is no simulator
 * specification needed when rerunning a simulated project
 * 
 * @author Michael Junker
 *
 */
public class RerunSimuComConfigurationTab extends SimuComConfigurationTab implements ILaunchConfigurationTab {

}
