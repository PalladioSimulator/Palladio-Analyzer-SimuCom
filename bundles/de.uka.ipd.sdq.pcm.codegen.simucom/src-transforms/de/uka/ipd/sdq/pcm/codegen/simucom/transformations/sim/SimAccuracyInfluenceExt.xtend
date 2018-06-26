package de.uka.ipd.sdq.pcm.codegen.simucom.transformations.sim

import org.palladiosimulator.analyzer.quality.QualityRepository
import org.palladiosimulator.analyzer.quality.parameters.ParameterReference
import org.palladiosimulator.analyzer.quality.parameters.pcm.PCMComponentParameterReference
import org.palladiosimulator.analyzer.quality.parameters.pcm.PCMOperationParameterReference
import org.palladiosimulator.analyzer.quality.parameters.pcm.PCMParameterReference
import org.palladiosimulator.analyzer.quality.parameters.pcm.PCMRequiredBusinessOperationReturnParameterReference
import org.palladiosimulator.analyzer.quality.qualityannotation.CharacterisedPCMParameterPartition
import org.palladiosimulator.analyzer.quality.qualityannotation.PCMServiceSpecification
import org.palladiosimulator.analyzer.quality.qualityannotation.QualityAnnotation
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF
import de.uka.ipd.sdq.pcm.transformations.Helper

class SimAccuracyInfluenceExt {
	QualityRepository qualityAnnotationRepository

	def String getResourceName(ResourceDemandingSEFF seff) {
		Helper::getResourceFileName(seff)
	}

	def String getResourceName(CharacterisedPCMParameterPartition partition) {
		Helper::getResourceFileName(partition)
	}

	// access to quality annotations
	def QualityRepository getQualityAnnotationRepository() {
		return this.qualityAnnotationRepository;
	}

	def QualityRepository setQualityAnnotationRepository(QualityRepository qualityAnnotationRepository) {
		this.qualityAnnotationRepository = qualityAnnotationRepository;
	}

	def QualityAnnotation getQualityAnnotation(ResourceDemandingSEFF rdseff) {
		if (qualityAnnotationRepository == null)
			return null
		else
			qualityAnnotationRepository.qualityStatements.filter(typeof(QualityAnnotation)).findFirst [ qa |
				(qa.forServiceSpecification instanceof PCMServiceSpecification) &&
					((qa.forServiceSpecification as PCMServiceSpecification).resourceDemandingSEFF == rdseff)
			]
	}

	// PCM parameter references to SimuCom String-based ids within RD-SEFFs
	def dispatch String getSimuComId(ParameterReference ref) '''
		ERROR: Unknown type of parameter reference.
	'''

	def dispatch String getSimuComId(PCMParameterReference ref) '''
		ERROR: Unknown type of PCM parameter reference.
	'''

	def dispatch String getSimuComId(PCMOperationParameterReference ref) {
		ref.parameter.parameterName
	}

	def dispatch String getSimuComId(PCMComponentParameterReference ref) '''
		ERROR: Unknown type of parameter reference.
	'''

	def dispatch String getSimuComId(PCMRequiredBusinessOperationReturnParameterReference ref) '''
		RETURN
	'''
}
