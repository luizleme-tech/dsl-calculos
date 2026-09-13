package com.luizlemetech.formula.model

import kotlinx.serialization.Serializable

@Serializable
data class CalculationGroup(
    val id: String,
    val variables: List<VariableDefinition>,
    val formulas: List<FormulaDefinition>,
    val outputs: List<String>
)
