package com.luizlemetech.formula.runtime

import com.luizlemetech.formula.model.CompiledCalculationGroup
import com.luizlemetech.formula.model.RuntimeValue
import com.luizlemetech.formula.model.VariableSource
import com.luizlemetech.formula.model.parserRuntimeValue
import kotlin.collections.forEach

class ExecutionContextFactory {

    fun create(
        group: CompiledCalculationGroup,
        input: Map<String, String>
    ): ExecutionContext {
        val initial = mutableMapOf<String, RuntimeValue>()

        group.variables
            .filter { it.source == VariableSource.CONSTANT }
            .forEach { variable ->
                val raw =
                        requireNotNull(variable.value) {
                            "Constante '${variable.name}' sem valor."
                        }

                initial[variable.name] = parserRuntimeValue(
                    variable.type,
                    raw
                )
            }

        val inputVariables =
            group.variables
                .filter { it.source == VariableSource.INPUT }

        inputVariables.forEach { variable ->
            val raw =
                input[variable.name]
                    ?: throw IllegalArgumentException("Variável '${variable.name}' não informada.")


            initial[variable.name] =
                parserRuntimeValue(
                    variable.type,
                    raw
                )
        }

        val allowed =
            inputVariables.map {it.name}.toSet()

        val unknown =
            input.keys - allowed

        require(unknown.isEmpty()) {
            "Inputs desconhecidos: $unknown"
        }

        return ExecutionContext(initial)
    }
}