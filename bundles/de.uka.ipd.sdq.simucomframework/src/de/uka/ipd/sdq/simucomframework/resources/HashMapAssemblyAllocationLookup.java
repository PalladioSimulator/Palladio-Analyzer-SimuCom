package de.uka.ipd.sdq.simucomframework.resources;

import java.util.Collections;
import java.util.Map;

/**
 * Provides a simple implementation of the interface based on HashMap. The
 * HashMap itself needs to be kept externally in order to provide editing
 * support.
 */
public class HashMapAssemblyAllocationLookup<AllocationType> implements IAssemblyAllocationLookup<AllocationType> {
    protected final Map<String, AllocationType> internalMap;

    /**
     * {@inheritDoc}
     */
    public HashMapAssemblyAllocationLookup(Map<String, AllocationType> delegateMap) {
        internalMap = Collections.unmodifiableMap(delegateMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllocationType getAllocatedEntity(String assemblyContextId) {
        return internalMap.get(assemblyContextId);
    }
}