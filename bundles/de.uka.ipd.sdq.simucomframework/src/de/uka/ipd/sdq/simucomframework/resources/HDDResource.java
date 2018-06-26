package de.uka.ipd.sdq.simucomframework.resources;

import java.io.Serializable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.resourceenvironment.HDDProcessingResourceSpecification;

import de.uka.ipd.sdq.simucomframework.Context;
import de.uka.ipd.sdq.simucomframework.SimuComSimProcess;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;

/**
 * Active resource representing a HDD resource with separate processing rate for write and read
 * speed in Bytes.
 *
 * @author tzwickl
 */
public class HDDResource extends ScheduledResource {
    private final int READ_SERVICE_ID = 1;
    private final int WRITE_SERVICE_ID = 2;
    private final HDDProcessingResourceSpecification hddResource;
    private static final Logger LOGGER = Logger.getLogger(HDDResource.class);

    public HDDResource(
            final HDDProcessingResourceSpecification activeResource,
            final SimuComModel myModel,
            final String resourceContainerID,
            final String schedulingStrategyID) {
        super(activeResource, myModel, resourceContainerID, schedulingStrategyID);
        this.hddResource = activeResource;
    }

    /* (non-Javadoc)
     * @see de.uka.ipd.sdq.simucomframework.resources.ScheduledResource#consumeResource(de.uka.ipd.sdq.simucomframework.SimuComSimProcess, int, java.util.Map, double)
     */
    @Override
    public void consumeResource(
            final SimuComSimProcess process,
            final int resourceServiceID,
            final Map<String, Serializable> parameterMap,
            final double abstractDemand) {
        double currentDemand = -1;
        switch (resourceServiceID) {
        case READ_SERVICE_ID:
            currentDemand = abstractDemand / Context.evaluateStatic(
                    this.hddResource.getReadProcessingRate().getSpecification(), Double.class);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("CALCULATED READ DEMAND: " + currentDemand + " FROM BYTES: " + abstractDemand);
            }
            break;
        case WRITE_SERVICE_ID:
            currentDemand = abstractDemand / Context.evaluateStatic(
                    this.hddResource.getWriteProcessingRate().getSpecification(), Double.class);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("CALCULATED WRITE DEMAND: " + currentDemand + " FROM BYTES: " + abstractDemand);
            }
            break;
        default:
            throw new RuntimeException("HDD Resource called without explicit read/write call");
        }
        super.consumeResource(process, resourceServiceID, parameterMap, currentDemand);
    }

}
