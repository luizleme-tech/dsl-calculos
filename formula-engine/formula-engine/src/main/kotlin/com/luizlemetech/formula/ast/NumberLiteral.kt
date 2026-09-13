package com.luizlemetech.formula.ast

import java.math.BigDecimal

data class NumberLiteral(
    val value: BigDecimal
) : Expression
