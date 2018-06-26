package de.uka.ipd.sdq.pcm.codegen.simucom.transformations.sim

import com.google.inject.Inject
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.RepositoryXpt
import org.palladiosimulator.pcm.repository.InfrastructureInterface
import org.palladiosimulator.pcm.repository.OperationInterface

class SimRepositoryXpt extends RepositoryXpt {
	@Inject SimJavaCoreXpt simJavaCoreXpt

	override interfaceHelperMethodsDeclarationTM(OperationInterface oi) {
		simJavaCoreXpt.interfaceHelperMethodsDeclaration(oi)
	}

	override interfaceHelperMethodsDeclarationTM(InfrastructureInterface ii) {
		simJavaCoreXpt.interfaceHelperMethodsDeclaration(ii)
	}

}
