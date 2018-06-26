package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import com.google.inject.Inject
import org.palladiosimulator.pcm.usagemodel.Branch
import org.palladiosimulator.pcm.usagemodel.Delay
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall
import org.palladiosimulator.pcm.usagemodel.Loop
import org.palladiosimulator.pcm.usagemodel.Start
import org.palladiosimulator.pcm.usagemodel.Stop

class UserActionsXpt {
	@Inject extension CallsXpt
	@Inject extension JavaNamesExt

	def dispatch userAction(Start start) ''''''

	def dispatch userAction(Stop stop) ''''''

	def dispatch userAction(EntryLevelSystemCall elsc) '''
		// Set the priority for «elsc.entityName»
		thread.setPriority(«elsc.priority»);
		«elsc.operationSignature__EntryLevelSystemCall.call(elsc,
			elsc.providedRole_EntryLevelSystemCall.portMemberVar + ".", elsc.inputParameterUsages_EntryLevelSystemCall,
			{
			})»
	'''

	def dispatch userAction(Loop loop) ''''''

	def dispatch userAction(Branch branch) ''''''

	def dispatch userAction(Delay delay) ''''''
}
