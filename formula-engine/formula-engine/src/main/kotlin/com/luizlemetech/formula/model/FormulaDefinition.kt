package com.luizlemetech.formula.model

import kotlinx.serialization.Serializable

@Serializable
data class FormulaDefinition(
    val name: String,
    val expression: String
)
