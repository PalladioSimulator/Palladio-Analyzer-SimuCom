package de.uka.ipd.sdq.pcm.codegen.simucom.transformations.sim

import com.google.inject.Inject
import org.palladiosimulator.pcm.core.entity.InterfaceProvidingEntity
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.JavaCoreXpt
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.JavaNamesExt
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.PCMext
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.SEFFBodyXpt
import org.palladiosimulator.pcm.repository.BasicComponent
import org.palladiosimulator.pcm.repository.ImplementationComponentType
import org.palladiosimulator.pcm.repository.InfrastructureInterface
import org.palladiosimulator.pcm.repository.InfrastructureSignature
import org.palladiosimulator.pcm.repository.OperationInterface
import org.palladiosimulator.pcm.repository.OperationSignature
import org.palladiosimulator.pcm.repository.RepositoryComponent
import org.palladiosimulator.pcm.repository.Signature
import org.palladiosimulator.pcm.seff.AbstractAction
import org.palladiosimulator.pcm.seff.BranchAction
import org.palladiosimulator.pcm.seff.CollectionIteratorAction
import org.palladiosimulator.pcm.seff.ForkAction
import org.palladiosimulator.pcm.seff.LoopAction
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF
import org.palladiosimulator.pcm.seff.StopAction

class SimJavaCoreXpt extends JavaCoreXpt {
	@Inject extension JavaNamesExt
	@Inject extension PCMext
	@Inject extension SEFFBodyXpt
	
	override specificImplementationPartTM(RepositoryComponent rc) '''
		// Component Parameter Defaults
		// TODO: The stackframes are not yet initialised by calling setComponentFrame in Protocom, thus initialize them here, too
		protected de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> myDefaultComponentStackFrame = new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();
	
		// Component Parameter setter
		// TODO: The stackframes are not yet initialised by calling setComponentFrame in Protocom, thus initialize them here, too
		protected de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> myComponentStackFrame = new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();
		
		public void setComponentFrame(de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> myComponentStackFrame) {
			this.myComponentStackFrame = myComponentStackFrame;	
			this.myDefaultComponentStackFrame = new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();
			«IF rc instanceof ImplementationComponentType»
				«FOR pu : (rc as ImplementationComponentType).componentParameterUsage_ImplementationComponentType»
					«FOR vc : pu.variableCharacterisation_VariableUsage»
						this.myDefaultComponentStackFrame.addValue("«pu.parameterUsageLHS()+'.'+vc.type.toString()»",
						   	new de.uka.ipd.sdq.simucomframework.variables.EvaluationProxy("«vc.specification_VariableCharacterisation.specification.specificationString()»",
						   	new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>()));
					«ENDFOR»
				«ENDFOR»
			«ENDIF»
		}
	'''
	
	override specificImplementationPartForInterfaceTM(RepositoryComponent rc) '''
		public void setComponentFrame(de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> myComponentStackFrame);
	'''
	
	
	def componentConstructor(RepositoryComponent rc) '''
		private static final org.apache.log4j.Logger logger = 
				org.apache.log4j.Logger.getLogger(de.uka.ipd.sdq.simucomframework.model.SimuComModel.class.getName());
				
		/** Default EMF factory for measuring points. */
		private static final org.palladiosimulator.edp2.models.measuringpoint.MeasuringpointFactory measuringpointFactory = org.palladiosimulator.edp2.models.measuringpoint.MeasuringpointFactory.eINSTANCE;
	
		
		private org.palladiosimulator.pcm.core.composition.AssemblyContext assemblyContext = null;
		private final String completeAssemblyContextID;
		private final java.util.Map<String,java.util.List<org.palladiosimulator.probeframework.probes.Probe>> startStopProbes = new java.util.HashMap<String,java.util.List<org.palladiosimulator.probeframework.probes.Probe>>();
		
		public org.palladiosimulator.pcm.core.composition.AssemblyContext getAssemblyContext() {
			return this.assemblyContext;
		}
		
		private de.uka.ipd.sdq.simucomframework.model.SimuComModel model;
	
		private de.uka.ipd.sdq.simucomframework.model.SimuComModel getModel() {
			return model;
		} 
		
		«rc.passiveResourceDecls»
		
		public «rc.javaName()» (String completeAssemblyContextID, String assemblyContextURI, de.uka.ipd.sdq.simucomframework.model.SimuComModel model) {
			this.completeAssemblyContextID = completeAssemblyContextID;
			this.assemblyContext = (org.palladiosimulator.pcm.core.composition.AssemblyContext) org.palladiosimulator.commons.emfutils.EMFLoadHelper.loadAndResolveEObject(assemblyContextURI);
			this.model = model;
			
			«rc.initCalculatorsTM»
		}
	'''
	
	def dispatch passiveResourceDecls(RepositoryComponent rc) '''
	'''
	
	def dispatch passiveResourceDecls(BasicComponent bc) '''
		// Initialize this component's passive resources
		«FOR pr : bc.passiveResource_BasicComponent»
			de.uka.ipd.sdq.scheduler.IPassiveResource pr_«pr.id.javaVariableName()» = null;
		«ENDFOR»
	'''
	
	def componentHelperMethodsDeclaration(InterfaceProvidingEntity ipe) '''
	 public org.palladiosimulator.pcm.core.composition.AssemblyContext getAssemblyContext();
	'''
	
	def dispatch interfaceHelperMethodsDeclaration(OperationInterface oi) '''
	 public org.palladiosimulator.pcm.core.composition.AssemblyContext getComponentAssemblyContext();
	'''
	
	def dispatch interfaceHelperMethodsDeclaration(InfrastructureInterface ii) '''
	 public org.palladiosimulator.pcm.core.composition.AssemblyContext getComponentAssemblyContext();
	'''
	
//	«REM»Template Method for the calculator initialization«ENDREM»
	@Inject SimCalculatorsXpt simCalculators
	def initCalculatorsTM(RepositoryComponent rc) {
		simCalculators.setupCalculators(rc)
	}
	
	def containerAvailabilityCheck(OperationSignature os) '''
		if (this.getModel().getConfiguration().getSimulateFailures()) {
			// Simulate a failure if one or multiple of the processing resources
			// required by the executing resource container are currently unavailable:
			de.uka.ipd.sdq.simucomframework.resources.AbstractSimulatedResourceContainer container = ctx.findResource(this.completeAssemblyContextID);
			java.util.List<de.uka.ipd.sdq.simucomframework.resources.AbstractScheduledResource> failedResources = container.getFailedResources();
			if(failedResources.size() > 0){
				double randValue = ctx.getModel().getConfiguration().getRandomGenerator().random();
				int index = (int)Math.floor(randValue * failedResources.size());
				de.uka.ipd.sdq.simucomframework.exceptions.FailureException.raise(
					this.getModel(),this.getModel().getFailureStatistics().getInternalHardwareFailureType(
								container.getResourceContainerID(),
								failedResources.get(index).getResourceTypeId()));
			}
		}
	'''
	
	/* Separation of bodies generated for actions and calling these bodies. This is 
	required due to the Java restriction that methods may not longer than 64k which 
	is easily violated if several external actions or infrastructure calls are 
	generated.
	Use ActionsAsCalls to execute the mapped AbstractAction. 
	Use ActionsAsMethods to generate the mapping for exactly one AbstractAction.
	*/
	def dispatch componentService(OperationSignature os, RepositoryComponent component) '''
		public «os.componentServiceSignature»
		{
			«os.containerAvailabilityCheckTM» 
«««		TODO: translate PROTECT
«««	      	«PROTECT CSTART '/*' CEND '*/' ID this.javaSignature() + "_" + component.id + "_" + this.interface__OperationSignature.id DISABLE»
			«IF os.hasSEFF(component)»
				«val seff = (os.getSEFF(component) as ResourceDemandingSEFF)»
				de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> resultStackFrame =
					new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();
				de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> methodBodyStackFrame =
					ctx.getStack().currentStackFrame();
				if (this.myDefaultComponentStackFrame.getContents().size() > 0) {
					methodBodyStackFrame.addVariables(this.myDefaultComponentStackFrame);
				}
				if (this.myComponentStackFrame.getContents().size() > 0) {
					methodBodyStackFrame.addVariables(this.myComponentStackFrame);
				}
				«seff.steps_Behaviour.findStart().actionsAsCalls("ctx")»
				return resultStackFrame;
			«ENDIF»
«««	   	 «ENDPROTECT»
	   }   
	   «IF os.hasSEFF(component)»
		«val seff = (os.getSEFF(component) as ResourceDemandingSEFF)»
	    	«seff.steps_Behaviour.findStart().actionsAsMethods»      
	  «ENDIF»
	'''
	
	def dispatch componentService(InfrastructureSignature is, RepositoryComponent component) '''
		public «is.componentServiceSignature»
		{ 
«««	      «PROTECT CSTART '/*' CEND '*/' ID this.javaSignature() + "_" + component.id + "_" + this.infrastructureInterface__InfrastructureSignature.id DISABLE»
«««		TODO: translate PROTECT
		«IF is.hasSEFF(component)»
		«val seff = (is.getSEFF(component) as ResourceDemandingSEFF)»
			de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> resultStackFrame =
				new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();
			de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> methodBodyStackFrame =
				ctx.getStack().currentStackFrame();
			if (this.myDefaultComponentStackFrame.getContents().size() > 0) {
				methodBodyStackFrame.addVariables(this.myDefaultComponentStackFrame);
			}
			if (this.myComponentStackFrame.getContents().size() > 0) {
				methodBodyStackFrame.addVariables(this.myComponentStackFrame);
			}
			«seff.steps_Behaviour.findStart().actionsAsCalls("ctx")»
			return resultStackFrame;   
		«ENDIF»
«««	   	  «ENDPROTECT»
	   }   
	   «IF is.hasSEFF(component)»
		«val seff = (is.getSEFF(component) as ResourceDemandingSEFF)»
	    	«seff.steps_Behaviour.findStart().actionsAsMethods»      
	  «ENDIF»
	'''
	
	def String actionsAsCalls(AbstractAction aa, String contextName) '''
		action«javaVariableName(aa.id)»(«contextName», resultStackFrame, methodBodyStackFrame);
	   «IF !(aa instanceof StopAction)»
	      «aa.successor_AbstractAction.actionsAsCalls(contextName)»
	   «ENDIF»
	'''
	
	def String actionsAsMethods(AbstractAction aa) '''
		private void action«javaVariableName(aa.id)»(
				final «aa.contextTypeTM» ctx, 
				final de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> resultStackFrame, 
				final de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> methodBodyStackFrame) {
			// EntityName = «javaString(aa.entityName)», Type = «javaString(aa.eClass.name)»
		   «aa.action»
		}
		«aa.actionsAsMethodsSubBehavior»
		«IF aa.successor_AbstractAction != null»
			«aa.successor_AbstractAction.actionsAsMethods»
		«ENDIF»
	'''
	
	def dispatch actionsAsMethodsSubBehavior(AbstractAction aa) '''
	'''
	
	def dispatch actionsAsMethodsSubBehavior(CollectionIteratorAction cia) '''
		«cia.bodyBehaviour_Loop.steps_Behaviour.findStart.actionsAsMethods»
	'''
	
	def dispatch actionsAsMethodsSubBehavior(LoopAction la) '''
		«la.bodyBehaviour_Loop.steps_Behaviour.findStart.actionsAsMethods»
	'''
	
	def dispatch actionsAsMethodsSubBehavior(BranchAction ba) '''
		«FOR transition : ba.branches_Branch.map[branchBehaviour_BranchTransition] SEPARATOR " "»
			«transition.steps_Behaviour.findStart.actionsAsMethods»
		«ENDFOR»
	'''
	
	def dispatch actionsAsMethodsSubBehavior(ForkAction fa) '''
		«FOR f : fa.asynchronousForkedBehaviours_ForkAction SEPARATOR " "»
			«f.steps_Behaviour.findStart().actionsAsMethods»
		«ENDFOR»
		«IF fa.synchronisingBehaviours_ForkAction != null»
			«FOR f : fa.synchronisingBehaviours_ForkAction.synchronousForkedBehaviours_SynchronisationPoint SEPARATOR " "»
				«f.steps_Behaviour.findStart().actionsAsMethods»
			«ENDFOR»
		«ENDIF»
	'''
	
	// overwritten template methods	
	override componentConstructorTM(RepositoryComponent component) {
		componentConstructor(component)
	}
	
	override componentHelperMethodsDeclarationTM(InterfaceProvidingEntity entity) {
		componentHelperMethodsDeclaration(entity)
	}
	
	override parameterListTM(Signature signature)      '''de.uka.ipd.sdq.simucomframework.SimuComContext ctx'''
	override parameterUsageListTM(Signature signature) '''ctx'''
	override returnTypeTM(Signature signature)         '''de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>'''
	override contextTypeTM(AbstractAction action)      '''de.uka.ipd.sdq.simucomframework.SimuComContext'''
	
	override componentServiceTM(Signature signature, RepositoryComponent component) {
		componentService(signature, component)
	}
	
	override containerAvailabilityCheckTM(OperationSignature signature) {
		containerAvailabilityCheck(signature)
	}
	
}