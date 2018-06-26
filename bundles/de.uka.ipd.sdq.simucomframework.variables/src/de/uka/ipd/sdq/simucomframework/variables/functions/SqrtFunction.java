package de.uka.ipd.sdq.simucomframework.variables.functions;

import java.util.List;

public class SqrtFunction implements IFunction {
    /** Name used in the stochastic expression for this function. */
    public static final String SQRT_FUNCTION_NAME = "Sqrt";

    @Override
    public boolean checkParameters(final List<Object> parameters) {
        if (parameters.size() != 1) {
            return false;
        }
        if (!(parameters.get(0) instanceof Double) && !(parameters.get(0) instanceof Integer)) {
            return false;
        }
        return true;
    }

    @Override
    public Object evaluate(final List<Object> parameters) {
        double value;
        if (parameters.get(0) instanceof Integer) {
            value = (Integer)parameters.get(0);
        } else {
            value = (Double)parameters.get(0);
        }

        return Math.sqrt(value);
    }

}
