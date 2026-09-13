package com.luizlemetech.formula.analysis

import com.luizlemetech.formula.ast.BinaryExpression
import com.luizlemetech.formula.ast.Expression
import com.luizlemetech.formula.ast.FunctionCall
import com.luizlemetech.formula.ast.NumberLiteral
import com.luizlemetech.formula.ast.UnaryExpression
import com.luizlemetech.formula.ast.VariableReference


class DependencyAnalyzer {

    fun findDependencies(
        expression: Expression
    ): Set<String> {
        val result = linkedSetOf<String>()
        collect(expression, result)
        return result
    }

    private fun collect(
        expression: Expression,
        result: MutableSet<String>
    ) {
        when(expression) {
            is NumberLiteral -> Unit

            is VariableReference ->
                result += expression.name

            is BinaryExpression ->  {
                collect(expression.left, result)
                collect(expression.right, result)
            }

            is UnaryExpression ->
                collect(expression.expression, result)

            is FunctionCall ->
                expression.arguments.forEach { collect(it, result) }

            else -> {}
        }
    }
}