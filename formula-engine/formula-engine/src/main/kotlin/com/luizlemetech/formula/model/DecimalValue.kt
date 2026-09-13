package com.luizlemetech.formula.model

import java.math.BigDecimal

data class DecimalValue(
    val value: BigDecimal
) : RuntimeValue {
    override fun type() = ValueType.DECIMAL
}
