package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import com.google.inject.Inject
import org.palladiosimulator.pcm.core.entity.Entity
import org.palladiosimulator.pcm.repository.BasicComponent
import org.palladiosimulator.pcm.repository.InfrastructureSignature
import org.palladiosimulator.pcm.repository.OperationSignature
import org.palladiosimulator.pcm.repository.RepositoryComponent
import org.palladiosimulator.pcm.seff.ExternalCallAction
import org.palladiosimulator.pcm.seff.InternalAction
import java.util.Set
import org.eclipse.emf.ecore.EObject

class SensorsExt {
	@Inject extension PCMext
	@Inject extension JavaNamesExt

	def String externalCallActionDescription(OperationSignature os, Object call) {
		"Call " + os.interface__OperationSignature.entityName + "." + os.javaSignature() + " <Component: " +
			(call as EObject).findContainerComponent().entityName +
			", AssemblyCtx: \"+this.assemblyContext.getId()+\", CallID: " + (call as ExternalCallAction).id + ">";
	}

	def String internalActionDescription(InfrastructureSignature os, Object call) {
		"Call " + os.infrastructureInterface__InfrastructureSignature.entityName + "." + os.javaSignature() +
			" <Component: " + (call as EObject).findContainerComponent().entityName +
			", AssemblyCtx: \"+this.assemblyContext.getId()+\", CallID: " + (call as InternalAction).id + ">";
	}

	def String entryLevelSystemCallActionDescription(OperationSignature os, Object call) {
		"Call_" + os.javaSignature() + " <EntryLevelSystemCall id: " + (call as Entity).id + " >";
	}

	def String entryLevelSystemCallActionDescription(InfrastructureSignature os, Object call) {
		"Call_" + os.javaSignature() + " <EntryLevelSystemCall id: " + (call as Entity).id + " >";
	}

	def Set<ExternalCallAction> getExternalCallActions(BasicComponent component) {
		val result = <ExternalCallAction>newHashSet

		component.serviceEffectSpecifications__BasicComponent.forEach [
			result.addAll(it.eAllContents.filter(typeof(ExternalCallAction)).toList)
		]

		result

	//	  component.serviceEffectSpecifications__BasicComponent.collect(e|e.eAllContents.select(action|ExternalCallAction.isInstance(action))).flatten();
	}

	def String seffDescription(OperationSignature os, RepositoryComponent component) {
		"SEFF " + os.interface__OperationSignature.entityName + "." + os.javaSignature() + " <Component: " +
			component.entityName + ", AssemblyCtx: \"+this.assemblyContext.getId()+\">";
	}

	def String seffDescription(InfrastructureSignature os, RepositoryComponent component) {
		"SEFF " + os.infrastructureInterface__InfrastructureSignature.entityName + "." + os.javaSignature() +
			" <Component: " + component.entityName + ", AssemblyCtx: \"+this.assemblyContext.getId()+\">";
	}
}
