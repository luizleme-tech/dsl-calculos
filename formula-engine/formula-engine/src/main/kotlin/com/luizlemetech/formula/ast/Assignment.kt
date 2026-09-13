package com.luizlemetech.formula.ast

data class Assignment(
    val target: String,
    val expression: Expression
)
