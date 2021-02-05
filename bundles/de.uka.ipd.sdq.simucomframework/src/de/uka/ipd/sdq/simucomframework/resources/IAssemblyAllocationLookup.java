package de.uka.ipd.sdq.simucomframework.resources;

import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.RepositoryComponent;

import de.uka.ipd.sdq.identifier.Identifier;

/**
 * Through the IAssemblyAllocationLookup interface it is possible to access the current allocations
 * of assembly contexts to resource containers.
 * 
 * The interface has been introduced to account for SimuLizar's dynamic nature with allocations
 * being created or changed during simulation time.
 * 
 * Furthermore, this interface allows for multiple contexts to share the same lookup information.
 * 
 * @author Sebastian Krach
 *
 */
public interface IAssemblyAllocationLookup<AllocationType> {

    /**
     * Get the entity to which the assembly context identified by the provided ID is allocated to.
     * 
     * @param assemblyContextId
     *            The UUID of the assembly context
     * @return the entity
     */
    AllocationType getAllocatedEntity(String assemblyContextId);

    /**
     * Get the entity to which the provided assembly context is allocated to.
     * 
     * @param context
     *            the assembly context
     * @return the entity
     */
    default AllocationType getAllocatedEntity(AssemblyContext context) {
        if (context.getParentStructure__AssemblyContext() instanceof RepositoryComponent) {
            throw new IllegalArgumentException("Only root assembly contexts can be allocated directly. "
                    + "Please use getAllocatedEntity(Stack<AssemblyContext>).");
        }
        return getAllocatedEntity(context.getId());
    }

    /**
     * Get the entity to which the provided assembly context within its hierarchical context is
     * allocated to.
     * 
     * @param contextHierarchy
     *            the hierarchy of assembly contexts of component compositions
     * @return the entity
     */
    default AllocationType getAllocatedEntity(List<AssemblyContext> contextHierarchy) {
        return getAllocatedEntity(contextHierarchy.stream()
            .map(Identifier::getId)
            .collect(Collectors.joining("::")));
    }

}
