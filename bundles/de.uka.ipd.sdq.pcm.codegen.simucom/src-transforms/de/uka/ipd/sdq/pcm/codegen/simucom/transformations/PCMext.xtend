package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import org.palladiosimulator.analyzer.completions.Completion
import org.palladiosimulator.pcm.core.composition.AssemblyConnector
import org.palladiosimulator.pcm.core.composition.AssemblyContext
import org.palladiosimulator.pcm.core.composition.AssemblyEventConnector
import org.palladiosimulator.pcm.core.composition.AssemblyInfrastructureConnector
import org.palladiosimulator.pcm.core.composition.ComposedStructure
import org.palladiosimulator.pcm.core.composition.Connector
import org.palladiosimulator.pcm.core.composition.DelegationConnector
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector
import org.palladiosimulator.pcm.core.composition.RequiredDelegationConnector
import org.palladiosimulator.pcm.core.entity.Entity
import org.palladiosimulator.pcm.core.entity.InterfaceProvidingEntity
import org.palladiosimulator.pcm.core.entity.InterfaceProvidingRequiringEntity
import org.palladiosimulator.pcm.parameter.VariableUsage
import org.palladiosimulator.pcm.repository.BasicComponent
import org.palladiosimulator.pcm.repository.CompositeComponent
import org.palladiosimulator.pcm.repository.InfrastructureProvidedRole
import org.palladiosimulator.pcm.repository.InfrastructureRequiredRole
import org.palladiosimulator.pcm.repository.OperationProvidedRole
import org.palladiosimulator.pcm.repository.OperationRequiredRole
import org.palladiosimulator.pcm.repository.ProvidedRole
import org.palladiosimulator.pcm.repository.RepositoryComponent
import org.palladiosimulator.pcm.repository.RequiredRole
import org.palladiosimulator.pcm.repository.Signature
import org.palladiosimulator.pcm.repository.SinkRole
import org.palladiosimulator.pcm.seff.AbstractAction
import org.palladiosimulator.pcm.seff.AbstractBranchTransition
import org.palladiosimulator.pcm.seff.AbstractLoopAction
import org.palladiosimulator.pcm.seff.BranchAction
import org.palladiosimulator.pcm.seff.ExternalCallAction
import org.palladiosimulator.pcm.seff.ForkAction
import org.palladiosimulator.pcm.seff.ForkedBehaviour
import org.palladiosimulator.pcm.seff.InternalAction
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF
import org.palladiosimulator.pcm.seff.StartAction
import org.palladiosimulator.pcm.seff.StopAction
import org.palladiosimulator.pcm.seff.SynchronisationPoint
import org.palladiosimulator.pcm.subsystem.SubSystem
import org.palladiosimulator.pcm.system.System
import de.uka.ipd.sdq.pcm.transformations.Helper
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction
import org.palladiosimulator.pcm.usagemodel.Branch
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall
import org.palladiosimulator.pcm.usagemodel.Loop
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour
import org.palladiosimulator.pcm.usagemodel.Stop
import org.palladiosimulator.pcm.usagemodel.UsageScenario
import de.uka.ipd.sdq.stoex.AbstractNamedReference
import de.uka.ipd.sdq.stoex.NamespaceReference
import de.uka.ipd.sdq.stoex.VariableReference
import java.util.ArrayList
import java.util.Collection
import java.util.HashSet
import java.util.List
import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.palladiosimulator.pcm.core.composition.RequiredInfrastructureDelegationConnector
import org.palladiosimulator.pcm.core.composition.ProvidedInfrastructureDelegationConnector

class PCMext {
	def findStart(List<AbstractAction> actions) {
		actions.filter(typeof(StartAction)).head
	}

	// select Connectors only, no DelegationConnectors
	def connectors(ComposedStructure s) {
		s.connectors__ComposedStructure.filter[connector|!(connector instanceof DelegationConnector)]
	}

	def dispatch test(Connector conn, AssemblyContext ctx, RequiredRole r) {

		// This should never be called
		false
	}

	def dispatch test(AssemblyConnector c, AssemblyContext ctx, RequiredRole r) {
		c.requiredRole_AssemblyConnector.id.equals(r.id) &&
			c.requiringAssemblyContext_AssemblyConnector.id.equals(ctx.id)
	}

	def dispatch test(AssemblyInfrastructureConnector c, AssemblyContext ctx, InfrastructureRequiredRole r) {
		c.requiredRole__AssemblyInfrastructureConnector.id.equals(r.id) &&
			c.requiringAssemblyContext__AssemblyInfrastructureConnector.id.equals(ctx.id)
	}

	def dispatch test(AssemblyEventConnector c, AssemblyContext ctx, SinkRole r) {
		c.sinkRole__AssemblyEventConnector.id.equals(r.id) &&
			c.sourceAssemblyContext__AssemblyEventConnector.id.equals(ctx.id)
	}

	def hasConnector(ComposedStructure s, AssemblyContext ctx, RequiredRole r) {
		connectors(s).filter[con|test(con, ctx, r)].size == 1
	}

	def getConnector(ComposedStructure s, AssemblyContext ctx, RequiredRole r) {
		connectors(s).filter[con|test(con, ctx, r)].head
	}

	def hasProvidedDelegationConnector(ComposedStructure s, ProvidedRole p) {
		s.connectors__ComposedStructure.filter(typeof(ProvidedDelegationConnector)).filter[dc|
			dc.outerProvidedRole_ProvidedDelegationConnector == p].size == 1
	}
	
	def hasProvidedInfrastructureDelegationConnector(ComposedStructure s, ProvidedRole p) {
		s.connectors__ComposedStructure.filter(typeof(ProvidedInfrastructureDelegationConnector)).filter[dc|
			dc.outerProvidedRole__ProvidedInfrastructureDelegationConnector == p].size == 1
	}

	def getProvidedDelegationConnector(ComposedStructure s, ProvidedRole p) {
		s.connectors__ComposedStructure.filter(typeof(ProvidedDelegationConnector)).filter[dc|
			dc.outerProvidedRole_ProvidedDelegationConnector == p].head
	}
	
	def getProvidedInfastructureDelegationConnector(ComposedStructure s, ProvidedRole p) {
		s.connectors__ComposedStructure.filter(typeof(ProvidedInfrastructureDelegationConnector)).filter[dc|
			dc.outerProvidedRole__ProvidedInfrastructureDelegationConnector == p].head
	}

	def hasRequiredDelegationConnector(ComposedStructure s, AssemblyContext ctx, RequiredRole r) {
		s.connectors__ComposedStructure.filter(typeof(RequiredDelegationConnector)).filter[dc|
			dc.innerRequiredRole_RequiredDelegationConnector == r].size == 1
	}
	
	def hasRequiredInfrastructureDelegationConnector(ComposedStructure s, AssemblyContext ctx, RequiredRole r) {
		s.connectors__ComposedStructure.filter(typeof(RequiredInfrastructureDelegationConnector)).filter[dc|
			dc.innerRequiredRole__RequiredInfrastructureDelegationConnector == r].size == 1
	}

	def getRequiredDelegationConnector(ComposedStructure s, AssemblyContext ctx, RequiredRole r) {
		s.connectors__ComposedStructure.filter(typeof(RequiredDelegationConnector)).filter[dc|
			dc.innerRequiredRole_RequiredDelegationConnector == r].head
	}
	
	def getRequiredInfrastructureDelegationConnector(ComposedStructure s, AssemblyContext ctx, RequiredRole r) {
		s.connectors__ComposedStructure.filter(typeof(RequiredInfrastructureDelegationConnector)).filter[dc|
			dc.innerRequiredRole__RequiredInfrastructureDelegationConnector == r].head
	}

	def hasProvidedInfrastructureDelegationConnector(ComposedStructure s, InfrastructureProvidedRole p) {
		s.connectors__ComposedStructure.filter(typeof(ProvidedDelegationConnector)).filter[dc|
			dc.outerProvidedRole_ProvidedDelegationConnector == p].size == 1
	}

	def getProvidedInfrastructureDelegationConnector(ComposedStructure s, InfrastructureProvidedRole p) {
		s.connectors__ComposedStructure.filter(typeof(ProvidedDelegationConnector)).filter[dc|
			dc.outerProvidedRole_ProvidedDelegationConnector == p].head
	}

	def hasRequiredInfrastructureDelegationConnector(ComposedStructure s, InfrastructureRequiredRole r) {
		s.connectors__ComposedStructure.filter(typeof(RequiredDelegationConnector)).filter[dc|
			dc.innerRequiredRole_RequiredDelegationConnector == r].size == 1
	}

	def getRequiredInfrastructureDelegationConnector(ComposedStructure s, InfrastructureRequiredRole r) {
		s.connectors__ComposedStructure.filter(typeof(RequiredDelegationConnector)).filter[dc|
			dc.innerRequiredRole_RequiredDelegationConnector == r].head
	}

	def List<EntryLevelSystemCall> querySystemCallsInLoops(ScenarioBehaviour scenBe) {
		scenBe.actions_ScenarioBehaviour.filter(typeof(Loop)).map[l|querySystemCalls(l.bodyBehaviour_Loop)].flatten().
			toList
	}

	def List<EntryLevelSystemCall> querySystemCallsInBranches(ScenarioBehaviour scenBe) {

		// TODO: test if translated correctly		
		scenBe.actions_ScenarioBehaviour.filter(typeof(Branch)).map[branchTransitions_Branch].flatten.map[
			querySystemCalls(it.branchedBehaviour_BranchTransition)].flatten.toList
	}

	def dispatch List<EntryLevelSystemCall> querySystemCalls(ScenarioBehaviour scenBe) {
		val result = new ArrayList<EntryLevelSystemCall>

		result.addAll(scenBe.actions_ScenarioBehaviour.filter(typeof(EntryLevelSystemCall)))
		result.addAll(scenBe.querySystemCallsInLoops)
		result.addAll(scenBe.querySystemCallsInBranches)

		result
	}

	def Set<System> getSystemsFromCalls(Collection<EntryLevelSystemCall> calls) {
		calls.map[c|c.providedRole_EntryLevelSystemCall.providingEntity_ProvidedRole as System].toSet
	}

	def dispatch List<EntryLevelSystemCall> querySystemCalls(UsageScenario us) {
		querySystemCalls(us.scenarioBehaviour_UsageScenario)
	}

	def dispatch String getID(VariableReference vr) {
		vr.referenceName
	}

	def dispatch String getID(AbstractNamedReference nsr) {
		'this is never called'
	}

	def dispatch String getID(NamespaceReference nsr) {
		nsr.referenceName + '.' + nsr.innerReference_NamespaceReference.getID
	}

	def parameterUsageLHS(VariableUsage vu) {
		vu.namedReference__VariableUsage.getID
	}

	def dispatch boolean isInnerReference(VariableReference vr) {
		vr.referenceName == "INNER"
	}

	def dispatch boolean isInnerReference(AbstractNamedReference nsr) {
		false
	}

	def dispatch boolean isInnerReference(NamespaceReference nsr) {
		nsr.referenceName == "INNER" || nsr.innerReference_NamespaceReference.isInnerReference() == true
	}

	def dispatch Set<Entity> collectRepositories(System s) {
		val result = new HashSet<Entity>

		result.addAll(
			s.assemblyContexts__ComposedStructure.map[encapsulatedComponent__AssemblyContext.collectRepositories].
				flatten)
		result.addAll(
			s.providedRoles_InterfaceProvidingEntity.filter(typeof(OperationProvidedRole)).map[it.collectRepositories].
				flatten)
		result.addAll(
			s.requiredRoles_InterfaceRequiringEntity.filter(typeof(OperationRequiredRole)).map[it.collectRepositories].
				flatten)
		result.addAll(
			s.requiredRoles_InterfaceRequiringEntity.filter(typeof(InfrastructureRequiredRole)).map[
				it.collectRepositories].flatten)
		result.addAll(
			s.providedRoles_InterfaceProvidingEntity.filter(typeof(InfrastructureProvidedRole)).map[
				it.collectRepositories].flatten)

		result
	}

	def dispatch Set<Entity> collectRepositories(OperationProvidedRole pr) {
		newHashSet(pr.providedInterface__OperationProvidedRole.repository__Interface as Entity)
	}

	def dispatch Set<Entity> collectRepositories(InfrastructureProvidedRole pr) {
		newHashSet(pr.providedInterface__InfrastructureProvidedRole.repository__Interface as Entity)
	}

	def dispatch Set<Entity> collectRepositories(OperationRequiredRole rr) {
		newHashSet(rr.requiredInterface__OperationRequiredRole.repository__Interface as Entity)
	}

	def dispatch Set<Entity> collectRepositories(InfrastructureRequiredRole rr) {
		newHashSet(rr.requiredInterface__InfrastructureRequiredRole.repository__Interface as Entity)
	}

	def dispatch Set<Entity> collectRepositories(RepositoryComponent pct) {
		newHashSet(pct.repository__RepositoryComponent as Entity)
	}

	def dispatch Set<Entity> collectRepositories(CompositeComponent cc) {
		val result = newHashSet(cc.repository__RepositoryComponent as Entity)
		result.addAll(
			cc.assemblyContexts__ComposedStructure.map[encapsulatedComponent__AssemblyContext.collectRepositories].
				flatten)
		result
	}

	def dispatch Set<Entity> collectRepositories(InterfaceProvidingRequiringEntity pct) {
		null
	}

	def hasSEFF(Signature service, RepositoryComponent c) {
		if (c instanceof BasicComponent)
			(c as BasicComponent).serviceEffectSpecifications__BasicComponent.filter[describedService__SEFF == service].
				size > 0
		else
			false
	}

	def getSEFF(Signature service, RepositoryComponent c) {
		(c as BasicComponent).serviceEffectSpecifications__BasicComponent.filter[e|e.describedService__SEFF == service].
			head
	}

	def Set<Completion> getAllCompletions(ComposedStructure s) {
		val Set<Completion> result = new HashSet<Completion>

		result.addAll(
			s.assemblyContexts__ComposedStructure.map[encapsulatedComponent__AssemblyContext].filter(typeof(Completion)))

		result.addAll(
			s.assemblyContexts__ComposedStructure.map[encapsulatedComponent__AssemblyContext].filter(
				typeof(ComposedStructure)).filter(comp|comp != null && !(comp instanceof Completion)).map[allCompletions].
				flatten)

		result
	}

	def AbstractAction findStopAction(AbstractAction a) {
		if (a instanceof StopAction)
			a
		else if (a.successor_AbstractAction != null)
			findStopAction(a.successor_AbstractAction)
		else
			null
	}

	def AbstractUserAction findStop(AbstractUserAction a) {
		if (a instanceof Stop)
			a
		else if (a.successor != null)
			findStop(a.successor)
		else
			null
	}

	def List<InterfaceProvidingEntity> getProvidingEntities(List<ProvidedRole> pr) {
		getProvidingEntitiesRecursive(pr, 0)
	}

	def List<InterfaceProvidingEntity> getProvidingEntitiesRecursive(List<ProvidedRole> pr, int i) {
		if (i < pr.size)
			if (getProvidingEntitiesRecursive(pr, i + 1).contains(pr.get(i).providingEntity_ProvidedRole))
				getProvidingEntitiesRecursive(pr, i + 1)
			else {
				val result = getProvidingEntitiesRecursive(pr, i + 1)
				result.add(pr.get(i).providingEntity_ProvidedRole)
				result
			}
		else
			emptyList
	}

	def BasicComponent findContainerComponent(EObject o) {
		if (o instanceof BasicComponent)
			o as BasicComponent
		else
			o.eContainer.findContainerComponent
	}

	def List<System> uniqueSystemList(List<System> s) {
		if (s != null) {
			recursiveList(s, newArrayList, 0)
		}
	}

	private def List<System> recursiveList(List<System> s, List<System> result, int pos) {
		if (pos < s.size) {
			if (s.get(pos) != null && result.contains(s.get(pos))) {
				recursiveList(s, result, pos + 1)
			} else {
				val element = s.get(pos) as System
				if (element != null) {
					result.add(element)
				}
				recursiveList(s, result, pos + 1)
			}
		} else {
			return result
		}
	}

	def String getParentSubsystemsIdConcatenationFor(System s, ComposedStructure toMatch) {
		val r = s.getParentIdConcatenationFor(toMatch);
		if (r == null)
			throw new Exception("Could not find parent structure for "+s.getId());
		return r.split(" ").last();
	}

	// Polymorphic switch: Execute getParentIdConcatenationFor for SubSystems and Systems.
	def private dispatch String getParentIdConcatenationFor(RepositoryComponent s, ComposedStructure toMatch) {
		""
	}

	def private dispatch String getParentIdConcatenationFor(System s, ComposedStructure toMatch) {
		s.getSystemParentIdConcatenationFor(toMatch)
	}

	def private dispatch String getParentIdConcatenationFor(SubSystem s, ComposedStructure toMatch) {
		s.getSystemParentIdConcatenationFor(toMatch)
	}
	
	def private dispatch String getParentIdConcatenationFor(Completion c, ComposedStructure toMatch) {
		c.getSystemParentIdConcatenationFor(toMatch)
	}

	def private String getSystemParentIdConcatenationFor(ComposedStructure s, ComposedStructure toMatch) {
		s.assemblyContexts__ComposedStructure.map[ac|ac.encapsulatedComponent__AssemblyContext.matchID(toMatch) + ac.id].
			findFirst[s2|s2.contains(toMatch.id)]
	}

	def private String matchID(RepositoryComponent s, ComposedStructure toMatch) {
		if (s.id.contains(toMatch.id))
			toMatch.id + " "
		else
			s.getParentIdConcatenationFor(toMatch)
	}

	// recursive query for ExternalCallActions within a RD-SEFF
	def dispatch List<ExternalCallAction> queryExternalCallActions(AbstractAction a, List<ExternalCallAction> result) {
		if (a.successor_AbstractAction != null)
			queryExternalCallActions(a.successor_AbstractAction, result)
		else
			result
	}

	def dispatch List<ExternalCallAction> queryExternalCallActions(ExternalCallAction a, List<ExternalCallAction> result) {
		if (a.successor_AbstractAction != null) {
			result.add(a)
			queryExternalCallActions(a.successor_AbstractAction, result)
		} else
			result
	}

	def dispatch List<ExternalCallAction> queryExternalCallActions(BranchAction a, List<ExternalCallAction> result) {
		queryExternalCallActions(a.branches_Branch, result)

		if (a.successor_AbstractAction != null)
			queryExternalCallActions(a.successor_AbstractAction, result)
		else
			result
	}

	def dispatch List<ExternalCallAction> queryExternalCallActions(List<AbstractBranchTransition> list,
		List<ExternalCallAction> result) {

		if (list.size > 0) {
			queryExternalCallActions(list.head.branchBehaviour_BranchTransition.steps_Behaviour.findStart(), result)
			queryExternalCallActions(list.tail.toList, result)
		} else
			result
	}

	def dispatch List<ExternalCallAction> queryExternalCallActions(AbstractLoopAction a, List<ExternalCallAction> result) {
		queryExternalCallActions(a.bodyBehaviour_Loop.steps_Behaviour.findStart(), result)
		if (a.successor_AbstractAction != null)
			queryExternalCallActions(a.successor_AbstractAction, result)
		else
			result
	}

	def dispatch List<ExternalCallAction> queryExternalCallActions(ForkAction a, List<ExternalCallAction> result) {
		queryExternalCallActionsForkedBehaviour(a.asynchronousForkedBehaviours_ForkAction, result)

		if (a.synchronisingBehaviours_ForkAction != null)
			queryExternalCallActions(a.synchronisingBehaviours_ForkAction, result)

		//		else
		//			emptyList
		if (a.successor_AbstractAction != null)
			queryExternalCallActions(a.successor_AbstractAction, result)
		else
			result
	}

	def dispatch List<ExternalCallAction> queryExternalCallActions(SynchronisationPoint p,
		List<ExternalCallAction> result) {
		queryExternalCallActionsForkedBehaviour(p.synchronousForkedBehaviours_SynchronisationPoint, result)
	}

	def List<ExternalCallAction> queryExternalCallActionsForkedBehaviour(List<ForkedBehaviour> list,
		List<ExternalCallAction> result) {
		if (list.size > 0) {
			queryExternalCallActions(list.head, result)
			queryExternalCallActionsForkedBehaviour(list.tail.toList, result)
		} else
			result
	}

	def dispatch List<ExternalCallAction> queryExternalCallActions(ForkedBehaviour b, List<ExternalCallAction> result) {
		queryExternalCallActions(b.steps_Behaviour.findStart(), result);
	}

	// recursive query for InternalActions within a RD-SEFF
	def dispatch List<InternalAction> queryInternalActions(AbstractAction a, List<InternalAction> result) {
		if (a.successor_AbstractAction != null)
			queryInternalActions(a.successor_AbstractAction, result)
		else
			result
	}

	def dispatch List<InternalAction> queryInternalActions(InternalAction a, List<InternalAction> result) {
		if (a.successor_AbstractAction != null) {
			val newResult = newArrayList(a)
			newResult.addAll(result)
			queryInternalActions(a.successor_AbstractAction, newResult)
		} else
			result
	}

	def dispatch List<InternalAction> queryInternalActions(BranchAction a, List<InternalAction> result) {
		queryInternalActions(a.branches_Branch, result)

		if (a.successor_AbstractAction != null)
			queryInternalActions(a.successor_AbstractAction, result)
		else
			result
	}

	def dispatch List<InternalAction> queryInternalActions(List<AbstractBranchTransition> list,
		List<InternalAction> result) {
		if (list.size > 0) {
			queryInternalActions(list.head.branchBehaviour_BranchTransition.steps_Behaviour.findStart(), result)
			queryInternalActions(list.tail.toList, result)
		} else
			result
	}

	def dispatch List<InternalAction> queryInternalActions(AbstractLoopAction a, List<InternalAction> result) {
		queryInternalActions(a.bodyBehaviour_Loop.steps_Behaviour.findStart(), result)

		if (a.successor_AbstractAction != null)
			queryInternalActions(a.successor_AbstractAction, result)
		else
			result
	}

	def dispatch List<InternalAction> queryInternalActions(ForkAction a, List<InternalAction> result) {
		queryInternalActionsForkedBehaviour(a.asynchronousForkedBehaviours_ForkAction, result)

		if (a.synchronisingBehaviours_ForkAction != null)
			queryInternalActions(a.synchronisingBehaviours_ForkAction, result)

		if (a.successor_AbstractAction != null)
			queryInternalActions(a.successor_AbstractAction, result)
		else
			result
	}

	def dispatch List<InternalAction> queryInternalActions(SynchronisationPoint p, List<InternalAction> result) {
		queryInternalActionsForkedBehaviour(p.synchronousForkedBehaviours_SynchronisationPoint, result)
	}

	def List<InternalAction> queryInternalActionsForkedBehaviour(List<ForkedBehaviour> list, List<InternalAction> result) {
		if (list.size > 0) {
			queryInternalActions(list.head, result)
			queryInternalActionsForkedBehaviour(list.tail.toList, result)
		} else
			result
	}

	def dispatch List<InternalAction> queryInternalActions(ForkedBehaviour b, List<InternalAction> result) {
		queryInternalActions(b.steps_Behaviour.findStart(), result)
	}

	def ResourceDemandingSEFF getRdseff(AbstractAction action) {
		Helper::getRdseff(action)
	}
}
