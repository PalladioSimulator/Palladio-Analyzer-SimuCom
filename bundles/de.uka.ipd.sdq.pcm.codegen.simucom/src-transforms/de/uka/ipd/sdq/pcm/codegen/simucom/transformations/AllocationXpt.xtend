package de.uka.ipd.sdq.pcm.codegen.simucom.transformations

import org.palladiosimulator.pcm.allocation.Allocation

abstract class AllocationXpt {
	def String allocationTM(Allocation allocation)
}