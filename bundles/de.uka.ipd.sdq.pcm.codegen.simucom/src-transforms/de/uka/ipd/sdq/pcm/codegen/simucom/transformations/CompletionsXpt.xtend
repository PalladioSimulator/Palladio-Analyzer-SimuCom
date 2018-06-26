package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import com.google.inject.Inject
import de.uka.ipd.sdq.pcm.codegen.simucom.helper.M2TFileSystemAccess
import org.palladiosimulator.pcm.repository.RepositoryComponent
import org.palladiosimulator.pcm.system.System
import org.palladiosimulator.analyzer.completions.CompletionRepository
import org.palladiosimulator.analyzer.completions.Completion

class CompletionsXpt {
	@Inject M2TFileSystemAccess fsa
	
	@Inject extension PCMext
	@Inject extension RepositoryXpt
	@Inject extension ComposedStructureXpt
	@Inject extension ProvidedPortsXpt
	@Inject extension ContextPatternXpt
	@Inject extension JavaNamesExt
	
	def root(CompletionRepository repository) {
		repository.completions_CompletionRepository.forEach[it.completionRoot]
	}

	def expandCompletions(System system) {
		system.allCompletions.forEach [
			completionRoot(it)
		]
	}

	def void completionRoot(Completion completion) {
		val fileName = completion.getFileName
		val fileContent = '''
			«completion.composedStructureStart»
			«completion.providedPorts»
			«completion.requiredInterfaces»
			«completion.composedStructureEnd»
		'''
		
		fsa.generateFile(fileName, fileContent)
		
		completion.assemblyContexts__ComposedStructure.map[encapsulatedComponent__AssemblyContext].forEach[innerCompletionComponent(it)]
	}
	
	def dispatch innerCompletionComponent(RepositoryComponent comp) '''
		«comp.componentRoot»
	'''
	
	def dispatch innerCompletionComponent(Completion comp) '''
		«comp.completionRoot»
	'''
}