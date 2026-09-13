import com.luizlemetech.formula.model.*
import com.luizlemetech.formula.model.ValueType.DECIMAL
import com.luizlemetech.formula.model.VariableSource.*
import com.luizlemetech.formula.runtime.CalculationEngine
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CalculationTests {

    @Test
    fun `deve calcular a area do triangulo`() {
        val group =
            CalculationGroup("tringule-area",
                variables = listOf(
                    VariableDefinition("base", INPUT, DECIMAL),
                    VariableDefinition("height", INPUT, DECIMAL),
                    VariableDefinition("two", CONSTANT, DECIMAL, "2"),
                    VariableDefinition("area", CALCULATED, DECIMAL)
                ),
                formulas = listOf(
                    FormulaDefinition("area", "area = (base * height) / two")
                ),
                outputs = listOf("area")
            )

        val request = CalculationRequest(
            items = listOf(mapOf("base" to "10", "height" to "8"))
        )

        val result = CalculationEngine()
            .execute(group, request)

        val area = result.items
            .single()
            .values
            .getValue("area") as DecimalValue

        assertEquals("40", area.value.stripTrailingZeros().toPlainString())
    }

    @Test
    fun `deve calcular energia cinetica`() {

        val group =
            CalculationGroup(
                id = "kinetic-energy",
                variables =
                    listOf(
                        VariableDefinition(
                            "mass",
                            VariableSource.INPUT,
                            ValueType.DECIMAL
                        ),
                        VariableDefinition(
                            "velocity",
                            VariableSource.INPUT,
                            ValueType.DECIMAL
                        ),
                        VariableDefinition(
                            "two",
                            VariableSource.CONSTANT,
                            ValueType.DECIMAL,
                            "2"
                        ),
                        VariableDefinition(
                            "energy",
                            VariableSource.CALCULATED,
                            ValueType.DECIMAL
                        )
                    ),
                formulas =
                    listOf(
                        FormulaDefinition(
                            "energy",
                            "energy = mass * velocity ^ 2 / two"
                        )
                    ),
                outputs =
                    listOf("energy")
            )

        val request =
            CalculationRequest(
                items =
                    listOf(
                        mapOf(
                            "mass" to "10",
                            "velocity" to "4"
                        )
                    )
            )

        val result =
            CalculationEngine()
                .execute(group, request)

        val energy =
            result.items
                .single()
                .values
                .getValue("energy") as DecimalValue

        assertEquals(
            "80",
            energy.value
                .stripTrailingZeros()
                .toPlainString()
        )
    }
}