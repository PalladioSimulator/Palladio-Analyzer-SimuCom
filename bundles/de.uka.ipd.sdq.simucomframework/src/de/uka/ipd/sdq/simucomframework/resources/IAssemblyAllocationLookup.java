package de.uka.ipd.sdq.simucomframework.resources;

import java.util.Collections;
import java.util.Map;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;

/**
 * Through the IAssemblyAllocationLookup interface it is possible to access the
 * current allocations of assembly contexts to resource containers.
 * 
 * The interface has been introduced to account for SimuLizar's dynamic nature
 * with allocations being created or changed during simulation time.
 * 
 * Furthermore, this interfaces allows for multiple contexts to share the same
 * lookup information.
 * 
 * @author Sebastian Krach
 *
 */
public interface IAssemblyAllocationLookup<AllocationType> {

	/**
	 * Provides a simple implementation of the interface based on HashMap. The
	 * HashMap itself needs to be kept externally in order to provide editing
	 * support.
	 */
	public class HashMapAssemblyAllocationLookup<AllocationType> implements IAssemblyAllocationLookup<AllocationType> {
		protected final Map<String, AllocationType> internalMap;

		public HashMapAssemblyAllocationLookup(Map<String, AllocationType> delegateMap) {
			internalMap = Collections.unmodifiableMap(delegateMap);
		}

		@Override
		public AllocationType getAllocatedEntity(String assemblyContextId) {
			return internalMap.get(assemblyContextId);
		}
	}

	/**
	 * Get the entity to which the assembly context identified by the provided ID is
	 * allocated to.
	 * 
	 * @param assemblyContextId The UUID of the assembly context
	 * @return the entity
	 */
	AllocationType getAllocatedEntity(String assemblyContextId);

	/**
	 * Get the entity to which the provided assembly context is allocated to.
	 * 
	 * @param context the assembly context
	 * @return the entity
	 */
	default AllocationType getAllocatedEntity(AssemblyContext context) {
		return getAllocatedEntity(context.getId());
	}
}
