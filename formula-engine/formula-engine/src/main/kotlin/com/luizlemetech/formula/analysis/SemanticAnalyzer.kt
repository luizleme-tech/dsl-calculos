package com.luizlemetech.formula.analysis

import com.luizlemetech.formula.model.CompiledCalculationGroup
import com.luizlemetech.formula.model.VariableSource

class SemanticAnalyzer {

    fun validate(group: CompiledCalculationGroup) {
        val names = group.variables.map { it.name }
        val uniqueNames = names.toSet()

        require(names.size == uniqueNames.size) { "Existem variáveis duplicadas" }

        group.variables
            .filter { it.source == VariableSource.CONSTANT }
            .forEach {
                require(it.value != null) {
                    "Constante '${it.name}' precisa possuir valor."
                }
            }

        val producers =
            group.formulas.map { it.produces }

        require(producers.size == producers.toSet().size) {
            "Mais de uma fórmula produz a mesma variável."
        }

        group.formulas.forEach { formula ->
            require(formula.produces in uniqueNames) {
                "Fórmula '${formula.name}' produz uma variável não declarada."
            }

            formula.requires.forEach {
                require(it in uniqueNames) {
                    "Fórmula '${formula.name}' requer uma variável não declarada '$it'."
                }
            }
        }

        group.variables
            .filter { it.source == VariableSource.CALCULATED}
            .forEach {
                require(it.name in producers) {
                    "Variável calculada '${it.name}' não é produzida por nenhuma fórmula."
                }
            }

        group.outputs.forEach {
            require(it in uniqueNames) {
                "Output '$it' não é uma variável declarada."
            }
        }
    }
}