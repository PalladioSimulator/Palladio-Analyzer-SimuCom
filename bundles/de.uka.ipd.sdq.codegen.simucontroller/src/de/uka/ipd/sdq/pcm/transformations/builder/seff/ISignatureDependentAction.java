package de.uka.ipd.sdq.pcm.transformations.builder.seff;

import org.palladiosimulator.pcm.repository.OperationSignature;

public interface ISignatureDependentAction {

    void setCurrentSignature(OperationSignature sig);

}