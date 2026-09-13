package com.luizlemetech.formula.runtime

import com.luizlemetech.formula.model.RuntimeValue

class ExecutionContext(
    initialValues: Map<String, RuntimeValue>
) {
    private val values = initialValues.toMutableMap()

    fun contains (name:String): Boolean = values.containsKey(name)

    fun get(name: String): RuntimeValue = values[name]
        ?: throw IllegalArgumentException("Variável $name não possui valor no contexto")

    fun set(
        name: String,
        value: RuntimeValue
    ) {
        values[name] = value
    }

    fun names(): Set<String> = values.keys.toSet()

    fun snapshot(): Map<String, RuntimeValue> = values.toMap()
}