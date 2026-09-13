package com.luizlemetech.formula.serialization

import com.luizlemetech.formula.model.CalculationGroup
import com.luizlemetech.formula.model.CalculationRequest
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path

class JsonLoader {

    private val json =
        Json {
            prettyPrint = true
            ignoreUnknownKeys = false
        }

    fun loadGroup(path: Path): CalculationGroup =
        json.decodeFromString(
            CalculationGroup.serializer(),
            Files.readString(path)
        )

    fun loadRequest(path: Path): CalculationRequest =
        json.decodeFromString(
            CalculationRequest.serializer(),
            Files.readString(path)
        )
}