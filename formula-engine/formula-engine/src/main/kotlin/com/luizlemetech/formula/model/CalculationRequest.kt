package com.luizlemetech.formula.model

import kotlinx.serialization.Serializable

@Serializable
data class CalculationRequest(
    val items: List<Map<String,String>>
)
