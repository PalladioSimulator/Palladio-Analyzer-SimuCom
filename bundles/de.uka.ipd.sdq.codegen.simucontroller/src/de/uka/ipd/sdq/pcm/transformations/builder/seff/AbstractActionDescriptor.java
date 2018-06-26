package de.uka.ipd.sdq.pcm.transformations.builder.seff;

import org.palladiosimulator.pcm.seff.AbstractAction;

public abstract class AbstractActionDescriptor {

    public AbstractActionDescriptor() {
        super();
    }

    public abstract AbstractAction createAction();
}