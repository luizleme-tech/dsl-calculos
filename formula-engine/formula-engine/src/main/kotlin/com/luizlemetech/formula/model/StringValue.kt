package com.luizlemetech.formula.model

import java.math.BigDecimal

data class StringValue(
    val value: String
) : RuntimeValue {
    override fun type() = ValueType.STRING
}

fun parserRuntimeValue(
    type: ValueType,
    rawValue: String
): RuntimeValue =
    when (type) {
        ValueType.INTEGER ->
            IntegerValue(rawValue.toBigInteger())
        ValueType.DECIMAL ->
            DecimalValue(rawValue.toBigDecimal())
        ValueType.STRING ->
            StringValue(rawValue)
    }

fun RuntimeValue.toBigDecimal(): BigDecimal =
    when (this) {
        is DecimalValue -> value
        is IntegerValue -> value.toBigDecimal()

        is StringValue ->
            throw IllegalArgumentException("Texto '$value' não pode participar de operações aritméticas")
    }
