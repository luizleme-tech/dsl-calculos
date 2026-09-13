package com.luizlemetech.formula.function

import com.luizlemetech.formula.model.DecimalValue
import com.luizlemetech.formula.model.RuntimeValue
import com.luizlemetech.formula.model.toBigDecimal
import java.math.BigDecimal
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private fun oneArgument(
    functionName: String,
    arguments: List<RuntimeValue>
): Double {
    require(arguments.size == 1) {
        "$functionName espera exatamente 1 argumento."
    }

    return arguments.single().toBigDecimal().toDouble()
}

class SqrtFunction : FormulaFunction {
    override val name = "sqrt"

    override fun execute(
        arguments: List<RuntimeValue>
    ): RuntimeValue {
        val value = oneArgument(name, arguments)

        require(value >= 0.0) {
            "raiz quadrada não aceita valor negativo nesta v1."
        }

        return DecimalValue(
            BigDecimal.valueOf(sqrt(value))
        )
    }
}

class SinFunction : FormulaFunction {
    override val name = "sin"

    override fun execute(arguments: List<RuntimeValue>) =
        DecimalValue(
            BigDecimal.valueOf(
                sin(oneArgument(name, arguments))
            )
        )
}

class CosFunction : FormulaFunction {
    override val name = "cos"

    override fun execute(arguments: List<RuntimeValue>) =
        DecimalValue(
            BigDecimal.valueOf(
                cos(oneArgument(name, arguments))
            )
        )
}

class TanFunction : FormulaFunction {
    override val name = "tan"

    override fun execute(arguments: List<RuntimeValue>) =
        DecimalValue(
            BigDecimal.valueOf(
                tan(oneArgument(name, arguments))
            )
        )
}

class AbsFunction : FormulaFunction {
    override val name = "abs"

    override fun execute(
        arguments: List<RuntimeValue>
    ): RuntimeValue {
        require(arguments.size == 1) {
            "abs espera exatamente 1 argumento."
        }

        return DecimalValue(
            arguments.single()
                .toBigDecimal()
                .abs()
        )
    }
}

class FunctionRegistry(
    functions: List<FormulaFunction> =
        listOf(
            SqrtFunction(),
            SinFunction(),
            CosFunction(),
            TanFunction(),
            AbsFunction()
        )
) {
    private val byName =
        functions.associateBy { it.name }

    fun execute(
        name: String,
        arguments: List<RuntimeValue>
    ): RuntimeValue =
        byName[name]
            ?.execute(arguments)
            ?: throw IllegalArgumentException(
                "Função '$name' não registrada."
            )

    fun contains(name: String): Boolean =
        name in byName
}