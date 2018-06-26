package de.uka.ipd.sdq.pcm.codegen.simucom.transformations.sim

import com.google.inject.Inject
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.JavaNamesExt
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.UsageXpt
import de.uka.ipd.sdq.pcm.codegen.simucom.transformations.UserActionsXpt
import org.palladiosimulator.pcm.usagemodel.Branch
import org.palladiosimulator.pcm.usagemodel.BranchTransition
import org.palladiosimulator.pcm.usagemodel.Delay
import org.palladiosimulator.pcm.usagemodel.Loop
import org.palladiosimulator.pcm.usagemodel.Start
import org.palladiosimulator.pcm.usagemodel.Stop

class SimUserActionsXpt extends UserActionsXpt {
	@Inject extension JavaNamesExt
	@Inject extension UsageXpt

	override dispatch userAction(Loop _this) '''
		for (int iterationCount_«_this.id.javaVariableName()» = 0, maxIterationCount_«_this.id.javaVariableName()» = (Integer)ctx.evaluate("«_this.
			loopIteration_Loop.specification.specificationString()»",Integer.class); 
			iterationCount_«_this.id.javaVariableName()» < maxIterationCount_«_this.id.javaVariableName()»; iterationCount_«_this.
			id.javaVariableName()»++){
		«_this.bodyBehaviour_Loop.actions_ScenarioBehaviour.filter(typeof(Start)).head.userActions»
		} 
	'''

	override dispatch userAction(Branch _this) '''
		{
			«val counterID = _this.id.javaVariableName»
			double u«counterID» = ctx.getModel().getConfiguration().getRandomGenerator().random();
			double sum«counterID» = 0;
			«FOR bt : _this.branchTransitions_Branch SEPARATOR ""»«bt.branchTransition(counterID)»«ENDFOR»
		}
	'''

	def branchTransition(BranchTransition _this, String counterNumber) '''
		if (sum«counterNumber» <= u«counterNumber» && u«counterNumber» < sum«counterNumber» + «_this.branchProbability» )
		{
			«_this.branchedBehaviour_BranchTransition.actions_ScenarioBehaviour.filter(typeof(Start)).head.userActions»
		}
		sum«counterNumber» += «_this.branchProbability»;
	'''

	override dispatch userAction(Delay _this) '''
		{
		double delay = de.uka.ipd.sdq.simucomframework.variables.converter.NumberConverter.toDouble(ctx.evaluate("«_this.
			timeSpecification_Delay.specification»"));
		ctx.getThread().hold(delay);
		}
	'''

	override dispatch userAction(Stop _this) '''
		'''
}
