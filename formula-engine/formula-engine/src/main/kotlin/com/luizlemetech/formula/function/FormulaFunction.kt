package com.luizlemetech.formula.function

import com.luizlemetech.formula.model.RuntimeValue

interface FormulaFunction {
    val name: String

    fun execute(
        arguments: List<RuntimeValue>
    ): RuntimeValue
}