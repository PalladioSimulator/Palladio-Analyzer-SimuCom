package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import com.google.inject.Inject
import de.uka.ipd.sdq.pcm.codegen.simucom.helper.M2TFileSystemAccess
import org.palladiosimulator.pcm.repository.InfrastructureInterface
import org.palladiosimulator.pcm.repository.InfrastructureSignature
import org.palladiosimulator.pcm.repository.OperationInterface
import org.palladiosimulator.pcm.repository.OperationSignature

class DelegatorClassXpt {
	@Inject M2TFileSystemAccess fsa
	
	@Inject extension JavaNamesExt
	@Inject extension ProvidedPortsXpt
	@Inject extension JavaCoreXpt
	
	def dispatch void delegatorClass(OperationInterface iface, String prefix) {
		val fileName = (iface.implementationPackage()+".delegates").fqnToDirectoryPath()+"/"+prefix+iface.javaName()+".java"
		val fileContent = '''
		package «iface.implementationPackage()».delegates;
		// Delegator class for interface «iface.entityName»
		public class «prefix»«iface.javaName()» implements «iface.fqn()»
		{
			private static org.apache.log4j.Logger logger = 
				org.apache.log4j.Logger.getLogger(«iface.fqn()».class.getName());
		
			protected «iface.fqn()» myInnerPort = null;
		     
			public «prefix»«iface.javaName()»(«iface.fqn()» myInnerPort){
				this.myInnerPort = myInnerPort;
			}
			
			«iface.composedComponentPortHelperMethodsTM»    

			«FOR subinterface : iface.signatures__OperationInterface»
			«subinterface.delegator»
			«ENDFOR»
		  }
		'''
		
		fsa.generateFile(fileName, fileContent)
	}
	
	def dispatch void delegatorClass(InfrastructureInterface iface, String prefix) {
		val fileName = (iface.implementationPackage()+".delegates").fqnToDirectoryPath()+"/"+prefix+iface.javaName()+".java"
		val fileContent = '''
			package «iface.implementationPackage()».delegates;
			// Delegator class for interface «iface.entityName»
			public class «prefix»«iface.javaName()» implements «iface.fqn()»
			{
				private static org.apache.log4j.Logger logger = 
					org.apache.log4j.Logger.getLogger(«iface.fqn()».class.getName());
			
				protected «iface.fqn()» myInnerPort = null;
			
				public «prefix»«iface.javaName()»(«iface.fqn()» myInnerPort){
					this.myInnerPort = myInnerPort;
				}
			
				«iface.composedComponentPortHelperMethodsTM»

			«FOR subinterface : iface.infrastructureSignatures__InfrastructureInterface»
			«subinterface.delegator»
			«ENDFOR»
			}
'''
		
		fsa.generateFile(fileName, fileContent)
	}
	
	def dispatch delegator(OperationSignature os) '''
		public «os.operationSignature» {
			logger.debug("Delegating «os.entityName»");
			// Pre
			ctx.setEvaluationMode(de.uka.ipd.sdq.simucomframework.variables.stoexvisitor.VariableMode.EXCEPTION_ON_NOT_FOUND);
			«os.returnTypeTM» result = myInnerPort.«os.javaSignature()»(
			     «os.parameterUsageListTM»);
			// Post
			ctx.setEvaluationMode(de.uka.ipd.sdq.simucomframework.variables.stoexvisitor.VariableMode.RETURN_DEFAULT_ON_NOT_FOUND);
		return result;
		}
	'''
	
	def dispatch delegator(InfrastructureSignature is) '''
		public «is.infrastructureSignature» {
			logger.debug("Delegating «is.entityName»");
			// Pre
			ctx.setEvaluationMode(de.uka.ipd.sdq.simucomframework.variables.stoexvisitor.VariableMode.EXCEPTION_ON_NOT_FOUND);
			«is.returnTypeTM» result = myInnerPort.«is.javaSignature()»(
			     «is.parameterUsageListTM»);
			// Post
			ctx.setEvaluationMode(de.uka.ipd.sdq.simucomframework.variables.stoexvisitor.VariableMode.RETURN_DEFAULT_ON_NOT_FOUND);
			return result;
		}
	'''
}