package de.uka.ipd.sdq.pcm.transformations.builder.resourceconsumer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.resourceenvironment.LinkingResource;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentFactory;
import org.palladiosimulator.pcm.resourcetype.CommunicationLinkResourceType;

import de.uka.ipd.sdq.pcm.transformations.builder.abstractbuilder.BasicComponentBuilder;
import de.uka.ipd.sdq.pcm.transformations.builder.seff.DelegatorComponentSeffBuilder;
import de.uka.ipd.sdq.pcm.transformations.builder.seff.StaticInternalActionDescriptor;
import de.uka.ipd.sdq.pcm.transformations.builder.util.PCMAndCompletionModelHolder;

/**
 * Generates a BasicComponent which determines and loads the underlying network resource with the
 * message's size of the message to transmit. Respects both requesting service and returning from
 * service. Assumes that the message size is contained in the variable stream.BYTESIZE.
 *
 * @author Steffen
 *
 */
public class NetworkLoadingComponentBuilder extends BasicComponentBuilder {

    private final Logger logger = Logger.getLogger(NetworkLoadingComponentBuilder.class);
    private final CommunicationLinkResourceType typeOfLink;

    /**
     * Constructor of the network load simulator component
     *
     * @param models
     *            Container for the PCM model instance to modify
     * @param interf
     *            Interface of the component, used to delgate the method calls to its target
     * @param linkingRes
     *            The linking resource on which the load is created
     */
    public NetworkLoadingComponentBuilder(final PCMAndCompletionModelHolder models, final OperationInterface interf,
            final LinkingResource linkingRes) {
        super(models, interf, null, "NetworkLoadingComponent");

        typeOfLink = linkingRes.getCommunicationLinkResourceSpecifications_LinkingResource()
                .getCommunicationLinkResourceType_CommunicationLinkResourceSpecification();
        final ResourceContainer dummyContainer = ResourceenvironmentFactory.eINSTANCE.createResourceContainer();
        dummyContainer.setId(linkingRes.getId());

        this.container = dummyContainer;
    }

    /**
     * Returns a SEFF builder which adds a network demand depending on the parameters passed
     */
    @Override
    protected DelegatorComponentSeffBuilder getSeffBuilder() {
        final DelegatorComponentSeffBuilder builder = new DelegatorComponentSeffBuilder(getOperationProvidedRole(),
                getOperationRequiredRole());

        // Network demand for the Request
        builder.appendPreAction(new StaticInternalActionDescriptor("stream.BYTESIZE", typeOfLink));

        // Network demand for the Reply
        builder.appendPostAction(new StaticInternalActionDescriptor("stream.BYTESIZE", typeOfLink));
        return builder;
    }

    @Override
    public void build() {
        if (logger.isEnabledFor(Level.INFO)) {
            logger.info("Creating a component to simulate network loads");
        }
        super.build();
    }
}
