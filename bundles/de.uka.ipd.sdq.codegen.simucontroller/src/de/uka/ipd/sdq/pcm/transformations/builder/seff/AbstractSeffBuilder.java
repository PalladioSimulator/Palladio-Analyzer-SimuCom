package de.uka.ipd.sdq.pcm.transformations.builder.seff;

import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;

public class AbstractSeffBuilder {

    public AbstractSeffBuilder() {
        super();
    }

    protected ResourceDemandingSEFF buildSeff(OperationSignature signature) {
        ResourceDemandingSEFF seff = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
        seff.setDescribedService__SEFF(signature);

        return seff;
    }

    protected AbstractAction createControlFlow(AbstractAction a, AbstractAction b) {
        a.setSuccessor_AbstractAction(b);
        return b;
    }

}