package de.uka.ipd.sdq.pcm.transformations.builder.seff;

import java.util.ArrayList;
import java.util.Collection;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.parameter.ParameterFactory;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisationType;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import de.uka.ipd.sdq.stoex.StoexFactory;
import de.uka.ipd.sdq.stoex.VariableReference;

public class MiddlewareComponentSeffBuilder extends DelegatorComponentSeffBuilder {

    protected OperationRequiredRole middlewareRole;

    public MiddlewareComponentSeffBuilder(OperationProvidedRole domainProvRole, OperationRequiredRole domainReqRole,
            OperationRequiredRole middlewareReqRole) {
        super(domainProvRole, domainReqRole);
        this.middlewareRole = middlewareReqRole;
    }

    public void appendPreMiddlewareCall(OperationSignature preSignature) {
        this.appendPreAction(new ExternalCallActionDescriptor(preSignature, this.middlewareRole,
                createMiddlewareParameter(), createOutParameter()));
    }

    public void appendPostMiddlewareCall(OperationSignature postSignature) {
        this.appendPostAction(new ExternalCallActionDescriptor(postSignature, this.middlewareRole,
                createMiddlewareParameter(), createOutParameter()));
    }

    protected Collection<VariableUsage> createMiddlewareParameter() {
        Collection<VariableUsage> result = new ArrayList<VariableUsage>();
        result.add(createVariableUsage("stream", VariableCharacterisationType.BYTESIZE, "stream.BYTESIZE"));
        return result;
    }

    protected VariableUsage createVariableUsage(String variableName, VariableCharacterisationType type, String spec) {
        VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
        VariableCharacterisation characterisation = ParameterFactory.eINSTANCE.createVariableCharacterisation();
        VariableReference name = StoexFactory.eINSTANCE.createVariableReference();
        name.setReferenceName(variableName);
        characterisation.setSpecification_VariableCharacterisation(CoreFactory.eINSTANCE.createPCMRandomVariable());
        characterisation.getSpecification_VariableCharacterisation().setSpecification(spec);
        characterisation.setType(type);
        usage.setNamedReference__VariableUsage(name);
        usage.getVariableCharacterisation_VariableUsage().add(characterisation);

        return usage;
    }

    private Collection<VariableUsage> createOutParameter() {
        Collection<VariableUsage> result = new ArrayList<VariableUsage>();
        result.add(createVariableUsage("stream", VariableCharacterisationType.BYTESIZE, "RETURN.BYTESIZE"));
        return result;
    }
}
