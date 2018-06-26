package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import com.google.inject.Inject
import org.palladiosimulator.pcm.core.entity.InterfaceProvidingEntity
import org.palladiosimulator.pcm.core.entity.InterfaceProvidingRequiringEntity
import de.uka.ipd.sdq.pcm.codegen.simucom.helper.M2TFileSystemAccess
import org.palladiosimulator.pcm.repository.BasicComponent
import org.palladiosimulator.pcm.repository.CompositeComponent
import org.palladiosimulator.pcm.repository.ImplementationComponentType
import org.palladiosimulator.pcm.repository.InfrastructureProvidedRole
import org.palladiosimulator.pcm.repository.InfrastructureSignature
import org.palladiosimulator.pcm.repository.OperationProvidedRole
import org.palladiosimulator.pcm.repository.OperationSignature
import org.palladiosimulator.pcm.repository.Parameter
import org.palladiosimulator.pcm.repository.RepositoryComponent
import org.palladiosimulator.pcm.repository.Signature
import org.palladiosimulator.pcm.seff.AbstractAction
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification
import org.palladiosimulator.pcm.seff.StopAction
import org.palladiosimulator.pcm.subsystem.SubSystem

abstract class JavaCoreXpt {
	@Inject M2TFileSystemAccess fsa
	
	@Inject extension JavaNamesExt
	@Inject extension DataTypesXpt
	@Inject extension SEFFBodyXpt
	@Inject extension ProvidedPortsXpt
	@Inject extension ContextPatternXpt
	
	def classHeader(RepositoryComponent component) '''
		public class «component.javaName»
	'''
	
	def operationSignature(OperationSignature signature) '''
		«signature.returnTypeTM»
		«signature.javaSignature»
		( «signature.parameterListTM» )
	'''	

	def infrastructureSignature(InfrastructureSignature signature) '''
		«signature.returnTypeTM»
		«signature.javaSignature»
		( «signature.parameterListTM» )
	'''	

	def dispatch returnType(OperationSignature signature) '''
		«IF signature.returnType__OperationSignature != null»
			«signature.returnType__OperationSignature.dataType»
		«ELSE»
			void
		«ENDIF»
	'''

	def dispatch returnType(InfrastructureSignature signature) '''
		void
	'''

	def dispatch returnTypeTM(OperationSignature signature) '''
		« /* ERROR: "OAW GENERATION ERROR [m2t_transforms/java_core.xpt]: ReturnTypeTM must be overridden" */ »
	'''

	def dispatch returnTypeTM(InfrastructureSignature signature) '''
		« /* ERROR: "OAW GENERATION ERROR [m2t_transforms/java_core.xpt]: ReturnTypeTM must be overridden" */ »
	'''

	def parameterListTM(Signature signature) '''
		« /* ERROR: "OAW GENERATION ERROR [m2t_transforms/java_core.xpt]: ParameterUsageTemplate method must be overridden" */ »
	'''

	def parameterUsageListTM(Signature signature) '''
		« /* ERROR "OAW GENERATION ERROR [m2t_transforms/java_core.xpt]: ParameterUsageTemplate method must be overridden" */ »
	'''
	
	def parameter(Parameter param) '''
		«IF param.dataType__Parameter != null»
			«param.dataType__Parameter.dataType»
		«ELSE»
			void
		«ENDIF»
		«param.parameterName»
	'''
	
	def parameterUse(Parameter param) '''
		«param.parameterName»
	'''

	//-----
	// Generate method implementations by traversing the SEFF
	def componentService(ServiceEffectSpecification seff) '''
	« /* ERROR "OAW GENERATION ERROR [m2t_transforms/java_core.xpt]: Unknown Service Effect Specification found!" */ »
	'''
	def dispatch componentServiceSignature(OperationSignature signature) '''
		«signature.returnTypeTM»	«signature.interface__OperationSignature.javaName.toFirstLower»_«signature.javaSignature»(«signature.parameterListTM»)
	'''

	def dispatch componentServiceSignature(InfrastructureSignature signature) '''
		«signature.returnTypeTM»	«signature.infrastructureInterface__InfrastructureSignature.javaName.toFirstLower»_«signature.javaSignature»(«signature.parameterListTM»)
	'''

	def dispatch componentServiceTM(OperationSignature signature, RepositoryComponent component) '''
	'''

	def dispatch componentServiceTM(InfrastructureSignature signature, RepositoryComponent component) '''
	'''


	def String actions(AbstractAction action) '''
		«action.action»
		«IF !(action instanceof StopAction)»
			«action.successor_AbstractAction.actions»
		«ENDIF»
	'''
	//-----
	
	//-----
	// Polymorphic switch to generate different thing for {Basic,Complete and Provides}-Types and for
	// ComposedStructure
	def dispatch componentImplementation(RepositoryComponent component) '''
		« /* ERROR "OAW GENERATION ERROR [m2t_transforms/java_core.xpt]: " + this.entityName +"(type "+this.metaType+") is an unknown RepositoryComponent type. Fix the transformations or contact the developers." */ »
	'''

	def dispatch componentImplementation(ImplementationComponentType component) '''
		«component.componentImplementationForImplComponentTypesAndSubSystems»
	'''

	def dispatch componentImplementation(SubSystem system) '''
		«system.componentImplementationForImplComponentTypesAndSubSystems»
	'''

	def componentImplementationForImplComponentTypesAndSubSystems(RepositoryComponent component) {
		val fileName = component.fileName
		val fileContent = '''
			«component.componentImplementationInterface»
			«component.componentPackage»
			
			«component.classHeader»
			«component.superClassesTM»
			implements «component.fqnInterface»
			{
				«component.componentConstructorTM»
				«component.providedPorts»
				«component.requiredInterfaces»
				«component.innerImplementation»
				«component.specificImplementationPartTM»
				
			}
			«component.componentImplementationChildClassTM»
		'''
		
		fsa.generateFile(fileName, fileContent)
	}

	def componentPackage(RepositoryComponent component) '''
		package «component.implementationPackage»;
	'''

	def componentImplementationInterface(InterfaceProvidingEntity entity) {
		val fileName = entity.fqnInterface.fqnToDirectoryPath+'.java'
		val fileContent = '''
			«entity.contentImplementationInterfaceHeader»
			{
				«entity.componentHelperMethodsDeclarationTM»				
				«entity.providedRoles_InterfaceProvidingEntity.filter(typeof(OperationProvidedRole)).
					map[providedInterface__OperationProvidedRole.signatures__OperationInterface].
					flatten.map[it.componentServiceSignature].join(";")»;
				«entity.providedRoles_InterfaceProvidingEntity.filter(typeof(InfrastructureProvidedRole)).
					map[providedInterface__InfrastructureProvidedRole.infrastructureSignatures__InfrastructureInterface].
					flatten.map[it.componentServiceSignature].join(";")»;
				«FOR providedRole:entity.providedRoles_InterfaceProvidingEntity.filter(typeof(OperationProvidedRole))»
					«providedRole.portGetterDefinition»
				«ENDFOR»
				«FOR providedRole:entity.providedRoles_InterfaceProvidingEntity.filter(typeof(InfrastructureProvidedRole))»
					«providedRole.portGetterDefinition»
				«ENDFOR»
				«IF (entity instanceof InterfaceProvidingRequiringEntity)»
					«{
						val requiringEntity = entity as InterfaceProvidingRequiringEntity
						requiringEntity.componentContextSetterDefinition
					}»
				«ENDIF»
				«IF (entity instanceof RepositoryComponent)»
					«{
						val requiringEntity = entity as RepositoryComponent
					 	requiringEntity.specificImplementationPartForInterfaceTM
					}»
				«ENDIF»
			}
		'''
		
		fsa.generateFile(fileName, fileContent)
	}
	
	def contentImplementationInterfaceHeader(InterfaceProvidingEntity entity) '''
		package «entity.implementationPackage»;
		
		public interface «entity.interfaceName»
	'''

	def componentHelperMethodsDeclarationTM(InterfaceProvidingEntity entity) '''
	'''

	def componentImplementationChildClassTM(RepositoryComponent component) '''
	'''

	def specificImplementationPartTM(RepositoryComponent component) '''
	'''

	// Add some component methods also to the generated java interface to access it via EJB lookup.
	def specificImplementationPartForInterfaceTM(RepositoryComponent component) '''
	'''
	
	def superClassesTM(RepositoryComponent component) '''
	'''
	
	def dispatch innerImplementation(RepositoryComponent component) '''
	'''
	
	def dispatch innerImplementation(BasicComponent component) '''
		«FOR iface:component.providedRoles_InterfaceProvidingEntity.filter(typeof(OperationProvidedRole)).
			map[providedInterface__OperationProvidedRole.signatures__OperationInterface].flatten»
			«componentServiceTM(iface, component)»
		«ENDFOR»
		«FOR iface:component.providedRoles_InterfaceProvidingEntity.filter(typeof(InfrastructureProvidedRole)).
			map[providedInterface__InfrastructureProvidedRole.infrastructureSignatures__InfrastructureInterface].flatten»
			«componentServiceTM(iface, component)»
		«ENDFOR»
	'''
	
	def dispatch innerImplementation(CompositeComponent component) '''
	'''

	def componentConstructorTM(RepositoryComponent component) '''
	'''

	def containerAvailabilityCheckTM(OperationSignature signature) '''
	'''

	def contextTypeTM(AbstractAction action) '''
	'''
}