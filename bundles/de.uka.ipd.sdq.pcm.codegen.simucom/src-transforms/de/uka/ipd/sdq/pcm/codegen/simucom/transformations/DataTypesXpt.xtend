package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import org.palladiosimulator.pcm.repository.CollectionDataType
import org.palladiosimulator.pcm.repository.CompositeDataType
import org.palladiosimulator.pcm.repository.DataType
import org.palladiosimulator.pcm.repository.PrimitiveDataType
import org.palladiosimulator.pcm.repository.PrimitiveTypeEnum

class DataTypesXpt {
	def dispatch String dataType(DataType type) {
		// «ERROR "OAW GENERATION ERROR [m2t_transforms/datatypes.xpt]: Unknown Datatype found"»
	}

	def dispatch String dataType(PrimitiveDataType type) {
		switch (type.type) {
			case PrimitiveTypeEnum::INT: "int"
			case PrimitiveTypeEnum::DOUBLE: "double"
			case PrimitiveTypeEnum::CHAR: "char"
			case PrimitiveTypeEnum::BYTE: "byte"
			case PrimitiveTypeEnum::STRING: "String"
		}
	}

	def dispatch String dataType(CollectionDataType type) '''
		«dataType(type.innerType_CollectionDataType)»[]
	'''

	def dispatch String dataType(CompositeDataType type) '''
		«type.entityName»
	'''
}
