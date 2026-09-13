package com.luizlemetech.formula.ast

data class BinaryExpression(
    val left: Expression,
    val operator: BinaryOperator,
    val right: Expression
) : Expression
