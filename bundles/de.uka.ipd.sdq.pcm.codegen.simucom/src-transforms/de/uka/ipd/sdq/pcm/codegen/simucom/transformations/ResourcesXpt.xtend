package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import org.palladiosimulator.pcm.repository.PassiveResource
import org.palladiosimulator.pcm.seff.AbstractInternalControlFlowAction
import org.palladiosimulator.pcm.seff.seff_performance.ParametricResourceDemand
import org.palladiosimulator.pcm.seff.seff_performance.ResourceCall

abstract class ResourcesXpt {
	// For each resource demand load a resource
	def resourceDemands(AbstractInternalControlFlowAction aicfa) '''
		«FOR action : aicfa.resourceDemand_Action»«action.resourceDemandTM»«ENDFOR»
		«FOR action : aicfa.resourceCall__Action»«action.resourceDemandTM»«ENDFOR»
	'''
	
	// What to do with the actual demand is target dependant
	def CharSequence resourceDemandTM(ParametricResourceDemand prd)

	def CharSequence resourceDemandTM(ResourceCall rc)
	
	def CharSequence passiveResourceInitTM(PassiveResource pr)
}