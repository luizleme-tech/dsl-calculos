package com.luizlemetech.formula.ast

data class FunctionCall(
    val name: String,
    val arguments: List<Expression>
) : Expression
