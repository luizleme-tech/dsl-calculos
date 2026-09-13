package com.luizlemetech.formula.compiler

import com.luizlemetech.formula.analysis.SemanticAnalyzer
import com.luizlemetech.formula.model.CalculationGroup
import com.luizlemetech.formula.model.CompiledCalculationGroup

class CalculationGroupCompiler(
    private val formulaCompiler: FormulaCompiler = FormulaCompiler(),
    private val semanticAnalyzer: SemanticAnalyzer = SemanticAnalyzer()
) {

    fun compile(
        group: CalculationGroup
    ): CompiledCalculationGroup {
        val compilied =
            CompiledCalculationGroup(
                id = group.id,
                variables = group.variables,
                formulas = group.formulas.map(formulaCompiler::compile),
                outputs = group.outputs
            )
        semanticAnalyzer.validate(compilied)
        return compilied
    }
}