package com.luizlemetech.formula.ast

data class UnaryExpression(
    val operator: UnaryOperator,
    val expression: Expression
) : Expression
