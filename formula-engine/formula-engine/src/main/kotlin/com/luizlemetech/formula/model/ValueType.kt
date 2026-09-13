package com.luizlemetech.formula.model

import kotlinx.serialization.Serializable

@Serializable
enum class ValueType {
    INTEGER,
    DECIMAL,
    STRING
}