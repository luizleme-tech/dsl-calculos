package com.luizlemetech.formula.model

import kotlinx.serialization.Serializable

@Serializable
data class VariableDefinition(
    val name: String,
    val source: VariableSource,
    val type: ValueType,
    val value: String? = null
)
