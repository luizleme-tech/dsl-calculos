package com.luizlemetech.formula.runtime

import com.luizlemetech.formula.analysis.ExecutionPlanner
import com.luizlemetech.formula.compiler.CalculationGroupCompiler
import com.luizlemetech.formula.model.CalculationGroup
import com.luizlemetech.formula.model.CalculationRequest
import com.luizlemetech.formula.model.CalculationResult
import com.luizlemetech.formula.model.ItemResult

class CalculationEngine(
    private val groupCompiler: CalculationGroupCompiler = CalculationGroupCompiler(),
    private val contextFactory: ExecutionContextFactory = ExecutionContextFactory(),
    private val planner: ExecutionPlanner = ExecutionPlanner(),
    private val evaluator: ExpressionEvaluator = ExpressionEvaluator()
) {

    fun execute(
        group: CalculationGroup,
        request: CalculationRequest
    ): CalculationResult {
        val compiledGroup = groupCompiler.compile(group)

        val results =
            request.items.map { input ->
                val context = contextFactory.create(compiledGroup, input)

                val plan = planner.plan(
                    formulas = compiledGroup.formulas,
                    initiallyAvailable = context.names()
                )

                plan.formulas.forEach { formula ->
                    val rawResult = evaluator.evaluate(formula.assignment.expression, context)

                    val target = compiledGroup.variables.first { it.name == formula.produces}

                    val result = coerceValue( rawResult, target.type)

                    context.set(formula.produces, result)
                }

                ItemResult(
                    values = compiledGroup.outputs.associateWith { context.get(it) }
                )
            }

        return CalculationResult(
            groupId = compiledGroup.id,
            items = results
        )
    }
}