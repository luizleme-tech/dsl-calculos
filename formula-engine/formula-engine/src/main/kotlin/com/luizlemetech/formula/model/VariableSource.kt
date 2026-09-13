package com.luizlemetech.formula.model

import kotlinx.serialization.Serializable

@Serializable
enum class VariableSource {
    CONSTANT,
    INPUT,
    CALCULATED
}