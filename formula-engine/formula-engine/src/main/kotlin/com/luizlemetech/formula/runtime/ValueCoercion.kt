package com.luizlemetech.formula.runtime

import com.luizlemetech.formula.model.DecimalValue
import com.luizlemetech.formula.model.IntegerValue
import com.luizlemetech.formula.model.RuntimeValue
import com.luizlemetech.formula.model.StringValue
import com.luizlemetech.formula.model.ValueType
import com.luizlemetech.formula.model.toBigDecimal

fun coerceValue(
    value: RuntimeValue,
    targetType: ValueType
): RuntimeValue =
    when (targetType) {
        ValueType.DECIMAL -> DecimalValue(value.toBigDecimal())

        ValueType.INTEGER -> IntegerValue(value.toBigDecimal().toBigIntegerExact())

        ValueType.STRING ->
            when (value) {
                is StringValue -> value
                is IntegerValue -> StringValue(value.value.toString())
                is DecimalValue -> StringValue(value.value.toPlainString())
            }
    }