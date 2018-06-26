/*******************************************************************************
 * Copyright (c) 2014 Kalray and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.palladiosimulator.simucom.swtbot.tests;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;

public class WaitForJobs implements ICondition {

    private final String mHumanReadableJobFamily;

    /**
     * Creates a condition that waits until all jobs of the given family are done.
     *
     * @param humanReadableJobFamily a human readable name for the job family.
     *   It may be null, it serves only for the failure message.
     */
    public WaitForJobs(final String humanReadableJobFamily) {
        this.mHumanReadableJobFamily = humanReadableJobFamily;
    }

    @Override
    public boolean test() throws Exception {
        final DebugPlugin plugin = DebugPlugin.getDefault();
        final ILaunchManager lm = plugin.getLaunchManager();
        boolean running = false;
        for (final IProcess p: lm.getLaunches()[0].getProcesses()){
            if (!p.isTerminated()) {
                running = true;
            }
        }
        return !running;
    }

    @Override
    public void init(final SWTBot bot) {
    }

    @Override
    public String getFailureMessage() {
        final String errMsg = "Wait for jobs failed: ";
        if (mHumanReadableJobFamily != null) {
            return mHumanReadableJobFamily + " jobs are still running.";
        }
        return errMsg;
    }

}
