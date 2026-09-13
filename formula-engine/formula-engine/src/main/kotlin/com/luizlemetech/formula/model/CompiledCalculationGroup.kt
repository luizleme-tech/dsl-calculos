package com.luizlemetech.formula.model

data class CompiledCalculationGroup(
    val id: String,
    val variables: List<VariableDefinition>,
    val formulas: List<CompiledFormula>,
    val outputs: List<String>
)
