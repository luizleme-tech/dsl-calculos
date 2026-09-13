package com.luizlemetech.formula.model

sealed interface RuntimeValue {
    fun type(): ValueType
}