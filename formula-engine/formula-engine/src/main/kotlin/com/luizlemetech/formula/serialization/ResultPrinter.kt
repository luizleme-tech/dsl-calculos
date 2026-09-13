package com.luizlemetech.formula.serialization

import com.luizlemetech.formula.model.CalculationResult
import com.luizlemetech.formula.model.DecimalValue
import com.luizlemetech.formula.model.IntegerValue
import com.luizlemetech.formula.model.RuntimeValue
import com.luizlemetech.formula.model.StringValue

class ResultPrinter {

    fun print(result: CalculationResult) {
        println("Grupo: ${result.groupId}")

        result.items.forEachIndexed { index, item ->
            println("Item ${index + 1}")

            item.values.forEach { (name, value) ->
                println("  $name = ${format(value)}")
            }
        }
    }

    private fun format(value: RuntimeValue): String =
        when (value) {
            is DecimalValue ->
                value.value
                    .stripTrailingZeros()
                    .toPlainString()

            is IntegerValue ->
                value.value.toString()

            is StringValue ->
                value.value
        }
}