package de.uka.ipd.sdq.simucomframework.resources;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;

import de.uka.ipd.sdq.scheduler.IActiveResource;
import de.uka.ipd.sdq.simucomframework.SimuComSimProcess;
import de.uka.ipd.sdq.simucomframework.exceptions.FailureException;
import de.uka.ipd.sdq.simucomframework.exceptions.ThroughputZeroOrNegativeException;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;
import de.uka.ipd.sdq.simucomframework.variables.StackContext;
import de.uka.ipd.sdq.simucomframework.variables.converter.NumberConverter;

/**
 * Realizes a LinkingResource. Adds the latency time to the passed demand in
 * {@link #consumeResource(SimuComSimProcess, double)}, and they is loaded by latency + demand /
 * throughput.
 * 
 * @author hauck, brosch, merkle, Sebastian Lehrig
 */
public class SimulatedLinkingResource extends AbstractScheduledResource {

    private static final Logger LOGGER = Logger.getLogger(SimulatedLinkingResource.class.getName());

    private static long resourceId = 1;

    private final LinkingResource linkingResource;
    private String throughput;
    private String latencySpec;
    //TODO use latency through DMBehavior, remove latencySpec Field
    //private DemandModifyingBehavior latency;

    // For resources that can fail (SimulatedLinkingResources):
    private final boolean canFail;
    private final double failureProbability;

    private boolean utilizationSet = false;

    // private SimpleTimeSpanSensor demandedTimeSensor;
    // private OverallUtilisationSensor utilisationSensor;

    public SimulatedLinkingResource(final LinkingResource linkingResource, final SimuComModel simuComModel,
            final String resourceContainerID) {
        super(simuComModel, linkingResource.getCommunicationLinkResourceSpecifications_LinkingResource()
                .getCommunicationLinkResourceType_CommunicationLinkResourceSpecification().getEntityName(), // typeID
                resourceContainerID, // resourceContainerID
                linkingResource.getCommunicationLinkResourceSpecifications_LinkingResource()
                        .getCommunicationLinkResourceType_CommunicationLinkResourceSpecification().getId(), // resourceTypeID
                linkingResource.getEntityName()
                        + " ["
                        + linkingResource.getCommunicationLinkResourceSpecifications_LinkingResource()
                                .getCommunicationLinkResourceType_CommunicationLinkResourceSpecification()
                                .getEntityName() + "] <" + linkingResource.getId() + ">", // description
                SchedulingStrategy.FCFS, 1, false);

        this.linkingResource = linkingResource;
        //TODO remove latencySpec field
        this.latencySpec = this.linkingResource.getCommunicationLinkResourceSpecifications_LinkingResource()
                .getLatency_CommunicationLinkResourceSpecification().getSpecification();
        //TODO latency as a demandModifyingBehavior:
        //this.latency = new DemandModifyingBehavior("1.0", latencySpec);
        //super.addDemandModifyingBehavior(this.latency);
        this.throughput = this.linkingResource.getCommunicationLinkResourceSpecifications_LinkingResource()
                .getThroughput_CommunicationLinkResourceSpecification().getSpecification();

        this.failureProbability = this.linkingResource.getCommunicationLinkResourceSpecifications_LinkingResource()
                .getFailureProbability();
        this.canFail = simuComModel.getConfiguration().getSimulateFailures() && failureProbability > 0.0;
    }

    public String getId() {
        return this.linkingResource.getId();
    }

    @Override
    protected IActiveResource createActiveResource(final SimuComModel simuComModel) {
        // this.demandedTimeSensor = new SimpleTimeSpanSensor(simuComModel,
        // "Demanded time at " + description);
        final IActiveResource aResource = getModel().getSchedulingFactory().createSimFCFSResource(
                SchedulingStrategy.FCFS.toString(), getNextResourceId());

        // utilisationSensor = new OverallUtilisationSensor(simuComModel,
        // "Utilisation of " + typeID + " " + description);
        return aResource;
    }

    @Override
    protected double calculateDemand(final double demand) {
        final double calculatedThroughput = NumberConverter.toDouble(StackContext.evaluateStatic(throughput));
        if (calculatedThroughput <= 0) {
            throw new ThroughputZeroOrNegativeException("Throughput at resource " + getName()
                    + " was less or equal zero");
        }

        //TODO remove adding latency if it is added as DemandModifyingBehavior
        final double result = NumberConverter.toDouble(StackContext.evaluateStatic(latencySpec)) + demand
                / calculatedThroughput;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("A network load of " + result + " has been determined.");
        }

        return result;
    }

    @Override
    protected void assertAvailability() {
    	// If the resource can fail, simulate a failure with the given
        // probability.
        // This works for communication link resources (LAN), but only if the
        // "simulate linking resources" option is activated. Otherwise, the
        // commlink failure is triggered out of the OAW generated code.

        if (this.canFail) {
            if (Math.random() < this.failureProbability) {
                FailureException.raise(this.getModel(), this.getModel().getFailureStatistics()
                        .getInternalNetworkFailureType(this.linkingResource.getId(), getResourceTypeId()));
            }
        }
    }

    @Override
    public double getRemainingDemandForProcess(final SimuComSimProcess thread) {
        return getUnderlyingResource().getRemainingDemand(thread);
    }

    @Override
    public void updateDemand(final SimuComSimProcess thread, final double demand) {
        getUnderlyingResource().updateDemand(thread, demand);
    }

    @Override
    public IActiveResource getScheduledResource() {
        return getUnderlyingResource();
        // return null;
    }

    @Override
    public void activateResource() {
        getUnderlyingResource().start();
    }

    @Override
    public void deactivateResource() {
        if (!utilizationSet) {
            // this.utilisationSensor.setTotalResourceDemand(totalDemandedTime,
            // 1);
            utilizationSet = true;
        }
        getUnderlyingResource().stop();
    }

    public static String getNextResourceId() {
        return "NETWORK_" + Long.toString(resourceId++);
    }

    public LinkingResource getLinkingResource() {
        return this.linkingResource;
    }

    /**
     * Change the linking resource throughput after its creation.
     * 
     * @param throughput the new throughput specification
     */
    public void setThroughput(String throughput) {
        this.throughput = throughput;
    }

    /**
     * Change the linking resource latency after its creation.
     * 
     * @param latency the new latency specification
     */
    public void setLatency(String latency) {
    	//TODO latency change through new DemandModifyingBehavior
    	//TODO remove this.latencySpec
//    	super.removeDemandModifyingBehavior(this.latency);
//    	this.latency = new DemandModifyingBehavior("1.0", latency);
//        super.addDemandModifyingBehavior(this.latency);
        this.latencySpec = latency;
    }
}