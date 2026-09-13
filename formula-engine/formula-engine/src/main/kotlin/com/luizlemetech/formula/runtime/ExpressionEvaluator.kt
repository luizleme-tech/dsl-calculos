package com.luizlemetech.formula.runtime

import com.luizlemetech.formula.ast.BinaryExpression
import com.luizlemetech.formula.ast.BinaryOperator
import com.luizlemetech.formula.ast.Expression
import com.luizlemetech.formula.ast.FunctionCall
import com.luizlemetech.formula.ast.NumberLiteral
import com.luizlemetech.formula.ast.UnaryExpression
import com.luizlemetech.formula.ast.UnaryOperator
import com.luizlemetech.formula.ast.VariableReference
import com.luizlemetech.formula.function.FunctionRegistry
import com.luizlemetech.formula.model.DecimalValue
import com.luizlemetech.formula.model.RuntimeValue
import com.luizlemetech.formula.model.toBigDecimal
import java.math.BigDecimal
import java.math.MathContext

class ExpressionEvaluator(
    private val functions: FunctionRegistry = FunctionRegistry(),
    private val mathContext: MathContext = MathContext.DECIMAL128
) {

    fun evaluate(
        expression: Expression,
        context: ExecutionContext
    ): RuntimeValue =
        when (expression) {
            is NumberLiteral ->
                DecimalValue(expression.value)

            is VariableReference ->
                context.get(expression.name)

            is UnaryExpression ->
                evaluateUnary(expression, context)

            is BinaryExpression ->
                evaluateBinary(expression, context)

            is FunctionCall ->
                functions.execute(
                    expression.name,
                    expression.arguments.map {
                        evaluate(it, context)
                    }
                )
        }

    private fun evaluateUnary(
        expression: UnaryExpression,
        context: ExecutionContext
    ): RuntimeValue {
        val value =
            evaluate(expression.expression, context).toBigDecimal()

        return when (expression.operator) {
            UnaryOperator.PLUS -> DecimalValue(value)
            UnaryOperator.MINUS -> DecimalValue(value.negate(mathContext))
        }
    }

    private fun evaluateBinary(
        expression: BinaryExpression,
        context: ExecutionContext
    ): RuntimeValue {
        val left =
            evaluate(expression.left, context).toBigDecimal()

        val right =
            evaluate(expression.right, context).toBigDecimal()

        val result =
            when (expression.operator) {
                BinaryOperator.ADD -> left.add(right, mathContext)

                BinaryOperator.SUBTRACT -> left.subtract(right, mathContext)

                BinaryOperator.MULTIPLY -> left.multiply(right, mathContext)

                BinaryOperator.DIVIDE -> {
                    require(right.compareTo(BigDecimal.ZERO) != 0) {
                        "Divisão por zero."
                    }
                    left.divide(right, mathContext)
                }

                BinaryOperator.POWER -> {
                    val normalized = right.stripTrailingZeros()

                    if (normalized.scale() <= 0) {
                        val exp = normalized.intValueExact()
                        if (exp >= 0) {
                            left.pow(exp, mathContext)
                        } else {
                            BigDecimal.ONE.divide(left.pow(-exp, mathContext), mathContext)
                        }
                    } else {
                        BigDecimal.valueOf(Math.pow(left.toDouble(), right.toDouble()))
                    }
                }
            }

        return DecimalValue(result)
    }
}