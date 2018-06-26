package de.uka.ipd.sdq.simucomframework.variables.functions;

import java.util.List;

public class LogFunction implements IFunction {
    /** Name used in the stochastic expression for this function. */
    public static final String LOG_FUNCTION_NAME = "Log";

    @Override
    public boolean checkParameters(final List<Object> parameters) {
        if (parameters.size() != 2) {
            return false;
        }

        // base
        if (parameters.get(0) instanceof Double) {
            final Double base = (Double) parameters.get(0);
            if(base<=0 || base==1) {
                return false;
            }
        }
        else if (parameters.get(0) instanceof Integer) {
            final Integer base = (Integer) parameters.get(0);
            if(base<=0 || base==1) {
                return false;
            }
        }
        else {
            return false;
        }

        // value
        if (parameters.get(1) instanceof Double) {
            final Double value = (Double) parameters.get(1);
            if(value <= 0) {
                return false;
            }
        }
        else if (parameters.get(1) instanceof Integer) {
            final Integer value = (Integer) parameters.get(1);
            if(value <= 0) {
                return false;
            }
        }
        else {
            return false;
        }

        return true;
    }

    @Override
    public Object evaluate(final List<Object> parameters) {
        Double base;
        if (parameters.get(0) instanceof Integer) {
            base = (double)(Integer)parameters.get(0);
        } else {
            base = (Double) parameters.get(0);
        }

        Double value;
        if (parameters.get(1) instanceof Integer) {
            value = (double)(Integer)parameters.get(1);
        } else {
            value = (Double) parameters.get(1);
        }

        return Math.log(value)/Math.log(base);
    }

}
