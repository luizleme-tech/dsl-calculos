package com.luizlemetech.formula.analysis

import com.luizlemetech.formula.model.CompiledFormula

data class ExecutionPlan(
    val formulas: List<CompiledFormula>
)

class ExecutionPlanner() {

    fun plan(formulas: List<CompiledFormula>,
             initiallyAvailable: Set<String>): ExecutionPlan {

        val available = initiallyAvailable.toMutableSet()

        val pending = formulas.toMutableList()

        val ordered = mutableListOf<CompiledFormula>()

        while (pending.isNotEmpty()) {
            val executable = pending.filter { available.containsAll(it.requires) }

            if (executable.isEmpty()) {
                val details = pending.joinToString("\n") {
                    "${it.name}: requires=${it.requires}; available=${available}"
                }

                throw IllegalStateException("Dependências não resolvidas ou ciclo detectado: \n$details")
            }

            executable.forEach {
                ordered += it
                available += it.produces
                pending.remove(it)
            }
        }
        return ExecutionPlan(ordered)
    }
}
