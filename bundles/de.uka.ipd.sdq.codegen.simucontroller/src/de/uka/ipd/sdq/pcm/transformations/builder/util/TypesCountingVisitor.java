package de.uka.ipd.sdq.pcm.transformations.builder.util;

import java.util.Stack;

import org.palladiosimulator.pcm.repository.CollectionDataType;
import org.palladiosimulator.pcm.repository.CompositeDataType;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.repository.PrimitiveDataType;
import org.palladiosimulator.pcm.repository.PrimitiveTypeEnum;
import org.palladiosimulator.pcm.repository.util.RepositorySwitch;

public class TypesCountingVisitor extends RepositorySwitch<String> {

    private PrimitiveTypeEnum type;
    private Stack<String> prefixes = new Stack<String>();

    public TypesCountingVisitor(Parameter p, PrimitiveTypeEnum type) {
        this.type = type;
        prefixes.push(p.getParameterName());
    }

    public TypesCountingVisitor(String name, PrimitiveTypeEnum type) {
        this.type = type;
        prefixes.push(name);
    }

    @Override
    public String caseCollectionDataType(CollectionDataType object) {
        String innerStoEx = this.doSwitch(object.getInnerType_CollectionDataType());
        if (innerStoEx != null) {
            if (innerStoEx.equals("1")) {
                return getPrefix() + ".NUMBER_OF_ELEMENTS";
            } else {
                // TODO: use sum instead for correct stochastic processing!
                return getPrefix() + ".NUMBER_OF_ELEMENTS * (" + innerStoEx + ")";
            }
        } else
            return null;
    }

    private String getPrefix() {
        String result = "";
        for (String s : prefixes) {
            result += s + ".";
        }
        return result.substring(0, result.length() - 1);
    }

    @Override
    public String caseCompositeDataType(CompositeDataType object) {
        return super.caseCompositeDataType(object);
    }

    @Override
    public String casePrimitiveDataType(PrimitiveDataType object) {
        if (object.getType() == this.type)
            return "1";
        return super.casePrimitiveDataType(object);
    }
}
