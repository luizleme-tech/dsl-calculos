package com.luizlemetech.formula.model

import com.luizlemetech.formula.ast.Assignment

data class CompiledFormula(
    val name: String,
    val source: String,
    val assignment: Assignment,
    val produces: String,
    val requires: Set<String>
)
