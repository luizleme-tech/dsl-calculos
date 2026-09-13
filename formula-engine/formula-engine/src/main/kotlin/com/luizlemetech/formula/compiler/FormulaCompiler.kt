package com.luizlemetech.formula.compiler

import com.luizlemetech.formula.analysis.DependencyAnalyzer
import com.luizlemetech.formula.model.CompiledFormula
import com.luizlemetech.formula.model.FormulaDefinition

class FormulaCompiler(
    private val parser: FormularParserFacade = FormularParserFacade(),
    private val astBuilder: AstBuilder = AstBuilder(),
    private val dependencyAnalyzer: DependencyAnalyzer = DependencyAnalyzer()
) {

    fun compile(
        definition: FormulaDefinition
    ): CompiledFormula {
        val parseTree = parser.parse(definition.expression)
        val assignment = astBuilder.build(parseTree)

        return CompiledFormula(
            name = definition.name,
            source = definition.expression,
            assignment = assignment,
            produces = assignment.target,
            requires = dependencyAnalyzer.findDependencies(assignment.expression)
        )
    }
}