package com.luizlemetech.formula.compiler

import com.luizlemetech.formula.antlr.FormulaParser
import com.luizlemetech.formula.ast.Assignment
import com.luizlemetech.formula.ast.BinaryExpression
import com.luizlemetech.formula.ast.BinaryOperator
import com.luizlemetech.formula.ast.Expression
import com.luizlemetech.formula.ast.FunctionCall
import com.luizlemetech.formula.ast.NumberLiteral
import com.luizlemetech.formula.ast.UnaryExpression
import com.luizlemetech.formula.ast.UnaryOperator
import com.luizlemetech.formula.ast.VariableReference
import java.math.BigDecimal

class AstBuilder {

    fun build(ctx: FormulaParser.AssignmentContext) : Assignment =
        Assignment(
            target = ctx.IDENTIFIER().text,
            expression = buildExpression(ctx.expression())
        )

    private fun buildExpression(
        ctx: FormulaParser.ExpressionContext
    ) : Expression =
        buildAdditive(ctx.additiveExpression())

    private fun buildAdditive(
        ctx: FormulaParser.AdditiveExpressionContext
    ) : Expression {
        val operands = ctx.multiplicativeExpression()
        var result = buildMultiplicative(operands.first())

        for (index in 1 until operands.size) {
            val symbol = ctx.getChild(index * 2 - 1)?.text
            val right = buildMultiplicative(operands[index])

            val operator = when (symbol) {
                "+" -> BinaryOperator.ADD
                "-" -> BinaryOperator.SUBTRACT
                else -> error("Operador aditivo inválido: $symbol")
            }

            result = BinaryExpression(result, operator, right)
        }
        return result
    }

    private fun buildMultiplicative(
        ctx: FormulaParser.MultiplicativeExpressionContext
    ):  Expression {
        val operands = ctx.powerExpression()
        var result = buildPower(operands.first())

        for (index in 1 until  operands.size) {
            val symbol = ctx.getChild(index * 2 -1)?.text
            val right  = buildPower(operands[index])

            val operator = when(symbol) {
                "*" -> BinaryOperator.MULTIPLY
                "/" -> BinaryOperator.DIVIDE
                else -> error("Operador multiplicativo inválido: $symbol")
            }

            result= BinaryExpression(result, operator, right)
        }
        return result
    }

    private fun buildPower(
        ctx: FormulaParser.PowerExpressionContext
    ): Expression {
        val left = buildUnary(ctx.unaryExpression())
        val right = ctx.powerExpression()

        return if (right == null)
            left
        else
            BinaryExpression(
                left = left,
                operator = BinaryOperator.POWER,
                right = buildPower(right)
            )
    }

    private fun buildUnary(
        ctx: FormulaParser.UnaryExpressionContext
    ) : Expression {
        val nested = ctx.unaryExpression()

        if (nested != null) {
            val operator = when(ctx.getChild(0)?.text) {
                "+" -> UnaryOperator.PLUS
                "-" -> UnaryOperator.MINUS
                else -> error("Operador unário inválido.")
            }
            return UnaryExpression(
                operator = operator,
                expression = buildUnary(nested)
            )
        }
        return buildPrimary(ctx.primaryExpression())
    }

    private fun buildPrimary(
        ctx: FormulaParser.PrimaryExpressionContext?
    ): Expression {
        ctx?.NUMBER()?.let {
            return NumberLiteral(BigDecimal(it.text))
        }

        ctx?.functionCall()?.let {
            return buildFunctionCall(it)
        }

        ctx?.IDENTIFIER()?.let {
            return VariableReference(it.text)
        }

        ctx?.expression()?.let {
            return buildExpression(it)
        }

        error("Expressão primária inválida: ${ctx?.text}")
    }

    private fun buildFunctionCall(
        ctx: FormulaParser.FunctionCallContext
    ): Expression =
        FunctionCall(
            name = ctx.IDENTIFIER().text,
            arguments =
                ctx.argumentList()?.expression()?.map(::buildExpression)?:emptyList()
        )
}