package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import com.google.inject.Inject
import org.palladiosimulator.analyzer.completions.DelegatingExternalCallAction
import org.palladiosimulator.pcm.reliability.FailureType
import org.palladiosimulator.pcm.reliability.HardwareInducedFailureType
import org.palladiosimulator.pcm.reliability.NetworkInducedFailureType
import org.palladiosimulator.pcm.reliability.ResourceTimeoutFailureType
import org.palladiosimulator.pcm.reliability.SoftwareInducedFailureType
import org.palladiosimulator.pcm.seff.AbstractAction
import org.palladiosimulator.pcm.seff.AcquireAction
import org.palladiosimulator.pcm.seff.BranchAction
import org.palladiosimulator.pcm.seff.CollectionIteratorAction
import org.palladiosimulator.pcm.seff.ExternalCallAction
import org.palladiosimulator.pcm.seff.ForkAction
import org.palladiosimulator.pcm.seff.InternalAction
import org.palladiosimulator.pcm.seff.LoopAction
import org.palladiosimulator.pcm.seff.ReleaseAction
import org.palladiosimulator.pcm.seff.SetVariableAction
import org.palladiosimulator.pcm.seff.StartAction
import org.palladiosimulator.pcm.seff.StopAction
import org.palladiosimulator.pcm.seff.seff_reliability.FailureHandlingEntity
import org.palladiosimulator.pcm.seff.seff_reliability.RecoveryAction
import org.palladiosimulator.pcm.seff.seff_reliability.RecoveryActionBehaviour

abstract class SEFFBodyXpt {
	@Inject extension CallsXpt
	@Inject extension PCMext
	@Inject extension ResourcesXpt
	@Inject extension JavaNamesExt
	@Inject extension JavaCoreXpt

	def dispatch String action(AbstractAction action) '''
		« /* ERROR */»
	'''	
	
	def dispatch String action(StartAction action) ''''''
	def dispatch String action(StopAction action) ''''''

	def dispatch String action(CollectionIteratorAction action) {
		// error
	}
	
	def dispatch String action(LoopAction action) {
		// error
	}
	def dispatch action(BranchAction action) '''«/* error */»	'''
	def dispatch action(AcquireAction action) '''«/* error */»	'''
	def dispatch action(ReleaseAction action) '''«/* error */»	'''
	def dispatch action(SetVariableAction action) '''«/* error */»	'''
	def dispatch action(ForkAction action) '''«/* error */»	'''
	def dispatch action(DelegatingExternalCallAction action) {
		// error
	}
	
	def dispatch action(InternalAction action) '''
	/* InternalAction - START */
		// software failures:
		«action.failureInternalActionPreTM»
		// direct resource demands:
		«action.resourceDemands»
		// infrastructure calls:
		«FOR call : action.infrastructureCall__Action»
			«call.call(action)»
		«ENDFOR»
	/* InternalAction - END */
	'''
	
	def dispatch action(ExternalCallAction action) '''
	/* ExternalCallAction - START */
		{ //this scope is needed if the same service is called multiple times in one SEFF. Otherwise there is a duplicate local variable definition.
			«action.calledService_ExternalService.call(action,
				"myContext.getRole"+action.role_ExternalService.javaName+"().",
				action.inputVariableUsages__CallAction,
				action.returnVariableUsage__CallReturnAction)»
		}
	/* ExternalCallAction - END */
	'''
	
	def dispatch action(RecoveryAction action) '''
	{ /* RecoveryAction - START */
		«action.primaryBehaviour__RecoveryAction.recoveryActionAlternative»
	} /* RecoveryAction - END */
	'''

	def String recoveryActionAlternative(RecoveryActionBehaviour behaviour) '''
	/* RecoveryActionBehaviour - START */
	«val id = behaviour.id.javaVariableName»
	«behaviour.initFailureHandling(id)»
	try {
		«behaviour.steps_Behaviour.findStart.actions»	
	} catch(de.uka.ipd.sdq.simucomframework.exceptions.FailureException ex_«id») {
			
		// Remember the type of the failure-on-demand occurrence:
		failureException_«id» = ex_«id»;

		// Remove all additional stack frames; they are invalid now:
		for(int frameCount_«id» = 0; frameCount_«id» < ctx.getStack().size() - stackSize_«id»; ++frameCount_«id») {
			ctx.getStack().removeStackFrame();
		}
	
		«FOR alternative:behaviour.failureHandlingAlternatives__RecoveryActionBehaviour»
			«alternative.nextRecoveryActionAlternative(id)»
		«ENDFOR»
	} finally {}

	// no more alternatives.
	if(failureException_«id»!=null) { // failure occurred? 
		throw failureException_«id»;
	}
	
	/* RecoveryActionBehaviour - END */
	'''
	
	def checkIfExceptionIsHandled(FailureHandlingEntity entity, String id) '''
		(
		«IF entity.failureTypes_FailureHandlingEntity.size == 0»
			false
		«ELSE»
			«entity.failureTypes_FailureHandlingEntity.map[checkFailureTypeMatch(it, id)].join("||")»
		«ENDIF»
		)
	'''
	
	def dispatch checkFailureTypeMatch(FailureType ft, String id) {
		// error
	}
	
	def dispatch checkFailureTypeMatch(SoftwareInducedFailureType ft, String id) '''
		«IF (ft instanceof ResourceTimeoutFailureType)»
			«val resourceFailureType = ft as ResourceTimeoutFailureType»
			(
			  (failureException_«id».getFailureType() instanceof
			  org.palladiosimulator.reliability.MarkovResourceTimeoutFailureType)
			  &&
			  (((org.palladiosimulator.reliability.MarkovResourceTimeoutFailureType)
			  failureException_«id».getFailureType()).getPassiveResourceId().equals(
			  "«resourceFailureType.passiveResource__ResourceTimeoutFailureType.id»"))
			)
		«ELSE»
			(
			  (failureException_«id».getFailureType() instanceof
			  org.palladiosimulator.reliability.MarkovSoftwareInducedFailureType)
			  &&
			  (((org.palladiosimulator.reliability.MarkovSoftwareInducedFailureType)
			  failureException_«id».getFailureType()).getSoftwareFailureId().equals("«ft.id»"))
			)
		«ENDIF»
	'''
	
	def dispatch checkFailureTypeMatch(HardwareInducedFailureType ft, String id) '''
		(
		  (failureException_«id».getFailureType() instanceof
		  org.palladiosimulator.reliability.MarkovHardwareInducedFailureType)
		  &&
		  (((org.palladiosimulator.reliability.MarkovHardwareInducedFailureType)
		  failureException_«id».getFailureType()).getResourceTypeId().equals(
		  "«ft.processingResourceType__HardwareInducedFailureType.id»"))
		)
	'''
	
	def dispatch checkFailureTypeMatch(NetworkInducedFailureType ft, String id) '''
		(
		  (failureException_«id».getFailureType() instanceof
		  org.palladiosimulator.reliability.MarkovNetworkInducedFailureType)
		  &&
		  (((org.palladiosimulator.reliability.MarkovNetworkInducedFailureType)
		  failureException_«id».getFailureType()).getCommLinkResourceTypeId().equals(
		  "«ft.communicationLinkResourceType__NetworkInducedFailureType.id»"))
		)
	'''
	
	def initFailureHandling(Object obj, String id) '''
		de.uka.ipd.sdq.simucomframework.exceptions.FailureException failureException_«id»=null;
		int stackSize_«id»=ctx.getStack().size();
	'''
	
	def nextRecoveryActionAlternative(RecoveryActionBehaviour behaviour, String id) '''
		// Let the next alternative behaviour handle the failure, if
		// (i)  the previous alternatives did not already handle it, and
		// (ii) the handled failure types of the next alternative include
		//      the occurred failure type:
		if(failureException_«id» != null)
		{
			if(«behaviour.checkIfExceptionIsHandled(id)») {
		
				// Mark the original exception as handled (even if the
				// handling alternative fails itself, this will be a new
				// failure, and the original failure is counted as handled):
				ctx.getModel().getFailureStatistics().increaseFailureCounter(org.palladiosimulator.reliability.FailureStatistics.FailureType.HANDLED, failureException_«id».getFailureType());
				failureException_«id» = null;
		
				«behaviour.recoveryActionAlternative»
			}
		}
	'''
	
	def catchFailureExceptions(ExternalCallAction action, String id) '''
		«IF action != null»
			catch(de.uka.ipd.sdq.simucomframework.exceptions.FailureException ex) {
				
				// Remember the type of the failure-on-demand occurrence:
				failureException_«id» = ex;
				
				// Remove all additional stack frames; they are invalid now:
				for(int frameCount_«id» = 0; frameCount_«id» < ctx.getStack().size() - stackSize_«id»; ++frameCount_«id») {
					ctx.getStack().removeStackFrame();
				}
			}
		«ELSE»
			finally {}
		«ENDIF»
	'''
	
		def failureInternalActionPreTM(InternalAction action) '''
		«/* nothing to do in the general case. */»
	'''
}