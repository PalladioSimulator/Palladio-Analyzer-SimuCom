package de.uka.ipd.sdq.simucomframework.fork;

import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simucomframework.SimuComSimProcess;
import de.uka.ipd.sdq.simucomframework.resources.AbstractSimulatedResourceContainer;
import de.uka.ipd.sdq.simucomframework.resources.IAssemblyAllocationLookup;
import de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStack;

/**
 * Context for forked behaviours
 * 
 * @author Steffen Becker
 *
 */
public class ForkContext extends Context {

    private final Context parentContext;

    /**
     * Constructor of the parallel process
     * 
     * @param parentContext
     *            The current context of the parent thread. Used to evaluate variables in the
     *            parallel process
     * @param parent
     *            The parent process
     */
    public ForkContext(final Context parentContext, final SimuComSimProcess parent) {
        super(parentContext.getModel());
        this.setSimProcess(parent);
        this.parentContext = parentContext;
        this.stack = new SimulatedStack<Object>();

        // Run this thread with a copy of the parents stackframe
        // Likely subject to change in later PCM versions
        this.stack.pushStackFrame(parentContext.getStack().currentStackFrame().copyFrame());
    }

    /**
     *
     */
    private static final long serialVersionUID = 6701742993106975705L;

    public Context getParentContext() {
        return parentContext;
    }

	@Override
	public IAssemblyAllocationLookup<AbstractSimulatedResourceContainer> getAssemblyAllocationLookup() {
		return getParentContext().getAssemblyAllocationLookup();
	}

}
