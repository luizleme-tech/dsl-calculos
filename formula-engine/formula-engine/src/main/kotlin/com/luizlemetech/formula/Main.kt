package com.luizlemetech.formula

import com.luizlemetech.formula.runtime.CalculationEngine
import com.luizlemetech.formula.serialization.JsonLoader
import com.luizlemetech.formula.serialization.ResultPrinter
import java.nio.file.Path

fun main(args: Array<String>) {
    require(args.size == 2) {
        "Uso: formula-engine <group.json> <request.json>"
    }

    val loader = JsonLoader()

    val group =
        loader.loadGroup(
            Path.of(args[0])
        )

    val request =
        loader.loadRequest(
            Path.of(args[1])
        )

    val result =
        CalculationEngine()
            .execute(
                group,
                request
            )

    ResultPrinter().print(result)
}
