package com.luizlemetech.formula.model

import java.math.BigInteger

data class IntegerValue(
    val value: BigInteger
) : RuntimeValue {
    override fun type() = ValueType.INTEGER
}

