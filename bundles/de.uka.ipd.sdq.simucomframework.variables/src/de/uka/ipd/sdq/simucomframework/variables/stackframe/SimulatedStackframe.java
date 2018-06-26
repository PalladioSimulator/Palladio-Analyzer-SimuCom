package de.uka.ipd.sdq.simucomframework.variables.stackframe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import de.uka.ipd.sdq.simucomframework.variables.exceptions.ValueNotInFrameException;

/**
 * A stackframe as used in compiler construction to realise variable scopes.
 * It is similar to a hashmap hashing variable IDs on variable values. However,
 * this map can query its parent maps if the value is not found in it
 * @author Steffen Becker
 *
 * @param <T>
 */
/**
 * @author Steffen Becker
 *
 * @param <T>
 */
public class SimulatedStackframe<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 547392494342021941L;

    private static final Logger LOGGER = Logger.getLogger(SimulatedStackframe.class.getName());

    private SimulatedStackframe<T> parentFrame = null;

    // Stackframe: Maps ID->Value
    private final HashMap<String, T> contents = new HashMap<String, T>();

    public SimulatedStackframe(final SimulatedStackframe<T> parent) {
        this.parentFrame = parent;
    }

    public SimulatedStackframe() {
        this.parentFrame = null;
    }

    /**
     * Add a value to this stackframe
     *
     * @param id
     *            ID of the value
     * @param value
     *            The actual value
     */
    public void addValue(final String id, final T value) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Value " + value + " added to stackframe under id " + id);
        }
        contents.put(id, value);
    }

    /**
     * Retrieve a value from this stackframe. If the value is not part of this stackframe the parent
     * stackframe is queried automatically
     *
     * @param id
     *            ID of the variable value to retrieve
     * @return The value of the variable with id ID.
     * @throws ValueNotInFrameException
     *             Is throw if this frame and all parent frames do not contain the id ID
     */
    public T getValue(final String id) throws ValueNotInFrameException {
        if (this.contents.containsKey(id)) {
            return this.contents.get(id);
        }
        if (parentFrame != null) {
            return parentFrame.getValue(id);
        }
        throw new ValueNotInFrameException("Identifier " + id + " not found in stackframe!");
    }

    /**
     * Clone this stackframe
     *
     * @return A clone of the stackframe. The parent frames are copies as well
     */
    public SimulatedStackframe<T> copyFrame() {
        final SimulatedStackframe<T> copy = new SimulatedStackframe<T>();
        for (final String key : this.contents.keySet()) {
            copy.addValue(key, contents.get(key));
        }
        if (parentFrame != null) {
            copy.setParentFrame(parentFrame.copyFrame());
        }
        return copy;
    }

    private void setParentFrame(final SimulatedStackframe<T> frame) {
        this.parentFrame = frame;
    }

    /**
     * @return All IDs and their value in the current frame. For debugging and error reporting cases
     */
    public ArrayList<Entry<String, T>> getContents() {
        return getContentsRecursive(new HashMap<String, T>());
    }

    private ArrayList<Entry<String, T>> getContentsRecursive(final HashMap<String, T> alreadyFound) {
        final ArrayList<Entry<String, T>> result = new ArrayList<Entry<String, T>>();
        for (final Entry<String, T> e : contents.entrySet()) {
            if (!alreadyFound.containsKey(e.getKey())) {
                alreadyFound.put(e.getKey(), e.getValue());
                result.add(e);
            }
        }
        if (parentFrame != null) {
            result.addAll(parentFrame.getContentsRecursive(alreadyFound));
        }
        return result;
    }

    /**
     * Add all variables and their values in the given frame to this frame
     *
     * @param callResult
     *            The frame whose contents will be copied into this frame
     */
    public void addVariables(final SimulatedStackframe<T> callResult) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Adding " + callResult.getContents().size() + " value(s) to own stackframe");
        }
        for (final Entry<String, T> e : callResult.contents.entrySet()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Adding " + e.getKey() + " with " + e.getValue());
            }
            this.addValue(e.getKey(), e.getValue());
        }

    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (final Entry<?,?> entry : contents.entrySet()) {
            sb.append("(");
            sb.append(entry.getKey().toString());
            sb.append(", ");
            sb.append(entry.getValue().toString());
            sb.append(")");
        }
        sb.append("]");
        return sb.toString();
    }
}
