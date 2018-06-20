package de.uka.ipd.sdq.simulation.abstractsimengine;

/**
 * The simulated model is the central class of a discrete-event system simulator.
 * <p>
 * Its {@link #init()} method is the entry point of each simulation run in that it schedules
 * initial events that put the simulation into operation. For this, the simulation model relies on a
 * simulation library (which is also referred to as simulation engine), which offers means to create
 * and schedule events (or processes, respectively), which are then executed at the intended
 * simulation time.
 * <p>
 * The methods {@link #getSimEngineFactory()} and {@link #getSimulationControl()} both provide
 * access to simulation library services.
 * <p>
 * Notice, that the term <i>model</i> refers to the circumstance that the simulation model is an
 * abstraction of a real-world system in that it imitates the system's behaviour.
 * 
 * @author Steffen Becker (this code has been factored out from SimuCom)
 * @author Philipp Merkle
 * @author Henning Groenda Clarification and maintenance on documentation.
 * 
 */
public interface ISimulationModel {

    /** Offers means to control the simulation run, e.g. to start and stop the simulation.
     * @return Controller for the event-based simulation.
     */
    public ISimulationControl getSimulationControl();

    public void setSimulationControl(ISimulationControl control);

    public void setSimulationEngineFactory(ISimEngineFactory factory);

    /** Creation of new elements, e.g. events and processes, in the simulation.
     * @return Factory to create element in the event-based simulation.
     */
    public ISimEngineFactory getSimEngineFactory();

    /**
     * Initialises the simulation. Especially, the initial events are scheduled here before the
     * simulation starts. If no events are scheduled, the simulator stops immediately after being
     * started.
     * <p>
     * This method is called directly before the simulation starts.
     */
    public void init();

    /**
     * Cleans up when the simulation stops.
     * <p>
     * This method is called directly after the simulation have stopped.
     */
    public void finalise();

    /**
     * Returns the configuration of the simulation run. Each running simulation model has a
     * configuration.
     */
    public ISimulationConfig getConfiguration();

}
