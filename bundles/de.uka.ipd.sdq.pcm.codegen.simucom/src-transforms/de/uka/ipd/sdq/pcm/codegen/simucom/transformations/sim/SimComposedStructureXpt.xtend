package de.uka.ipd.sdq.pcm.codegen.simucom.transformations.sim

import com.google.inject.Inject
import org.palladiosimulator.pcm.core.composition.AssemblyContext
import org.palladiosimulator.pcm.core.composition.ComposedStructure
import org.palladiosimulator.pcm.core.entity.ComposedProvidingRequiringEntity
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.ComposedStructureXpt
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.JavaNamesExt
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.PCMext
import org.palladiosimulator.pcm.repository.BasicComponent
import org.palladiosimulator.pcm.repository.CompositeComponent
import org.palladiosimulator.pcm.subsystem.SubSystem
import org.palladiosimulator.pcm.system.System
import org.palladiosimulator.analyzer.completions.Completion

class SimComposedStructureXpt extends ComposedStructureXpt {
	@Inject extension PCMext
	@Inject extension JavaNamesExt

	override childInitTM(AssemblyContext ac, ComposedStructure s) '''
		context.setUserData(this.myContext.getUserData());
		  
		«IF ac.encapsulatedComponent__AssemblyContext instanceof BasicComponent»
			//Initialize Component Parameters
			de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object> componentStackFrame = 
				new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>();
			«FOR pu : ac.configParameterUsages__AssemblyContext»
				«FOR vc : pu.variableCharacterisation_VariableUsage»
					componentStackFrame.addValue("«pu.parameterUsageLHS() + '.' + vc.type.toString()»",
					   	new de.uka.ipd.sdq.simucomframework.variables.EvaluationProxy("«vc.specification_VariableCharacterisation.
			specification.specificationString()»",
					   	new de.uka.ipd.sdq.simucomframework.variables.stackframe.SimulatedStackframe<Object>()));
				«ENDFOR»
			«ENDFOR»
			
			// possibly overwrite some with user data if this AssemblyContext is meant
			this.myContext.getUserData().overwriteParametersForAssemblyContext(my«ac.javaName()».getAssemblyContext().getId(), componentStackFrame);
			
			my«ac.javaName()».setComponentFrame(componentStackFrame);
		«ENDIF»
	'''

	def composedPREConstructorStart(ComposedProvidingRequiringEntity cpre) '''
		private org.palladiosimulator.pcm.core.composition.AssemblyContext assemblyContext = null;
		
	public org.palladiosimulator.pcm.core.composition.AssemblyContext getAssemblyContext() {
		return this.assemblyContext;
	}	
		
		private de.uka.ipd.sdq.simucomframework.model.SimuComModel model;
		
		private de.uka.ipd.sdq.simucomframework.model.SimuComModel getModel() {
			return model;
		}
	
	«««		open curly brace here, is closed in main define.
		«IF cpre instanceof System»
		public «cpre.className()»(de.uka.ipd.sdq.simucomframework.model.SimuComModel model) {
		this.model = model; 
		«ELSE»
		public «cpre.className()» (String completeAssemblyContextID, String assemblyContextURI, de.uka.ipd.sdq.simucomframework.model.SimuComModel model) {
	    this.assemblyContext = (org.palladiosimulator.pcm.core.composition.AssemblyContext) org.palladiosimulator.commons.emfutils.EMFLoadHelper.loadAndResolveEObject(assemblyContextURI);
	
		
		this.model = model;
				
		logger.info("Creating composed structure «cpre.entityName» with AssemblyContextID " + assemblyContext.getId());
		«ENDIF»
	««« 	Do not close curly brace here.
	'''

	private def childMemberVarInit(AssemblyContext ac) '''
		my«ac.javaName()» = new «ac.encapsulatedComponent__AssemblyContext.fqn()»
		      	(«ac.componentConstructorParametersTM», model);
	'''

	// overwritten template methods
	override composedPREConstructorStartTM(ComposedProvidingRequiringEntity entity) {
		composedPREConstructorStart(entity)
	}

	override componentConstructorParametersTM(AssemblyContext obj) {
		if ((obj.parentStructure__AssemblyContext instanceof CompositeComponent) ||
			(obj.parentStructure__AssemblyContext instanceof SubSystem) ||
			(obj.parentStructure__AssemblyContext instanceof Completion))
			'''"«obj.id»" + completeAssemblyContextID, "«obj.eResource.URI + '#' + obj.id»"'''
		else if (obj.parentStructure__AssemblyContext instanceof System)
			'''"«obj.id»", "«obj.eResource.URI + '#' + obj.id»"'''
		else
			'''this.assemblyContext'''
	}

	override childMemberVarInitTM(AssemblyContext context) {
		childMemberVarInit(context)
	}

	override composedStructureEnd(ComposedStructure entity) '''
		}
	'''

}
