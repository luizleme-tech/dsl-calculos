# Motor Genérico de Fórmulas em Kotlin + ANTLR --- Implementação Completa V1

## Objetivo

Este documento parte de uma pasta vazia e chega a uma V1 funcional do
motor genérico de fórmulas. A implementação inclui projeto Kotlin, ANTLR
gerando `.kt`, gramática, Parse Tree, AST, `AstBuilder`, compilação,
tipos, constantes, inputs, variáveis calculadas, dependências, contexto
de execução, funções matemáticas, JSON, execução em lote e testes.

O motor não conhece "círculo", "Bhaskara", "Pitágoras" ou "física". Ele
conhece uma linguagem matemática e executa grupos configurados.

> **ANTLR + Kotlin:** para gerar Lexer/Parser em `.kt`, esta V1 usa o
> target/runtime Kotlin mantido pela Strumenta (`antlr-kotlin` 1.0.12).
> Ele é um target comunitário, não um dos targets oficiais do projeto
> ANTLR. Para este laboratório isso é intencional.

------------------------------------------------------------------------

# 1. Modelo mental

``` text
CalculationGroup JSON
        │
        ├── variables
        │     ├── CONSTANT
        │     ├── INPUT
        │     └── CALCULATED
        │
        ├── formulas
        └── outputs
        │
        ▼
FormulaCompiler
        │
        ├── Lexer
        ├── Parser
        ├── Parse Tree
        ├── AstBuilder
        └── AST
        │
        ▼
DependencyAnalyzer
        │
        ▼
ExecutionPlanner
        │
Request JSON ───────┐
Constants ──────────┤
                    ▼
            ExecutionContext
                    │
                    ▼
            ExpressionEvaluator
                    │
                    ▼
                 Result
```

Teremos duas classificações independentes.

**Origem da variável:**

``` text
CONSTANT
INPUT
CALCULATED
```

**Tipo do valor:**

``` text
INTEGER
DECIMAL
STRING
```

Assim:

``` text
pi
source = CONSTANT
type   = DECIMAL
value  = 3.14

r
source = INPUT
type   = DECIMAL

area
source = CALCULATED
type   = DECIMAL
```

------------------------------------------------------------------------

# 2. Estrutura do projeto

``` text
formula-engine/
├── antlr/
│   └── Formula.g4
├── examples/
│   ├── circle-group.json
│   ├── circle-request.json
│   ├── quadratic-group.json
│   └── quadratic-request.json
├── src/
│   ├── main/kotlin/br/com/luizleme/formula/
│   │   ├── Main.kt
│   │   ├── ast/Ast.kt
│   │   ├── analysis/DependencyAnalyzer.kt
│   │   ├── analysis/ExecutionPlanner.kt
│   │   ├── analysis/SemanticAnalyzer.kt
│   │   ├── compiler/AstBuilder.kt
│   │   ├── compiler/CalculationGroupCompiler.kt
│   │   ├── compiler/FormulaCompiler.kt
│   │   ├── compiler/FormulaParserFacade.kt
│   │   ├── function/FormulaFunction.kt
│   │   ├── function/StandardFunctions.kt
│   │   ├── model/CalculationModels.kt
│   │   ├── model/RuntimeValue.kt
│   │   ├── runtime/CalculationEngine.kt
│   │   ├── runtime/ExecutionContext.kt
│   │   ├── runtime/ExecutionContextFactory.kt
│   │   ├── runtime/ExpressionEvaluator.kt
│   │   ├── runtime/ValueCoercion.kt
│   │   ├── serialization/JsonLoader.kt
│   │   └── serialization/ResultPrinter.kt
│   └── test/kotlin/br/com/luizleme/formula/
│       └── CalculationEngineTest.kt
├── build.gradle.kts
└── settings.gradle.kts
```

------------------------------------------------------------------------

# 3. Criando o projeto

## `settings.gradle.kts`

``` kotlin
rootProject.name = "formula-engine"
```

## `build.gradle.kts`

``` kotlin
import com.strumenta.antlrkotlin.gradle.AntlrKotlinTask

plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.serialization") version "2.4.10"
    id("com.strumenta.antlr-kotlin") version "1.0.12"
    application
}

group = "br.com.luizleme"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.strumenta:antlr-kotlin-runtime-jvm:1.0.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

val generateKotlinGrammarSource =
    tasks.register<AntlrKotlinTask>("generateKotlinGrammarSource") {
        dependsOn("cleanGenerateKotlinGrammarSource")

        source = fileTree(layout.projectDirectory.dir("antlr")) {
            include("**/*.g4")
        }

        packageName = "br.com.luizleme.formula.antlr"
        arguments = listOf("-visitor", "-no-listener")

        outputDirectory =
            layout.buildDirectory
                .dir("generatedAntlr/${packageName.replace(".", "/")}")
                .get()
                .asFile
    }

sourceSets {
    main {
        kotlin.srcDir(generateKotlinGrammarSource)
    }
}

tasks.compileKotlin {
    dependsOn(generateKotlinGrammarSource)
}

application {
    mainClass.set("br.com.luizleme.formula.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
```

> Se o import de `AntlrKotlinTask` não for resolvido pela versão do
> plugin instalada, use o autocomplete do Gradle para a classe
> `AntlrKotlinTask`. A configuração acima segue a API documentada do
> plugin; o pacote dessa classe é o ponto mais suscetível a mudança
> entre versões.

------------------------------------------------------------------------

# 4. A gramática

Crie `antlr/Formula.g4`:

``` antlr
grammar Formula;

assignment
    : IDENTIFIER ASSIGN expression EOF
    ;

expression
    : additiveExpression
    ;

additiveExpression
    : multiplicativeExpression
      ((PLUS | MINUS) multiplicativeExpression)*
    ;

multiplicativeExpression
    : powerExpression
      ((MULTIPLY | DIVIDE) powerExpression)*
    ;

powerExpression
    : unaryExpression
      (POWER powerExpression)?
    ;

unaryExpression
    : (PLUS | MINUS) unaryExpression
    | primaryExpression
    ;

primaryExpression
    : NUMBER
    | functionCall
    | IDENTIFIER
    | LPAREN expression RPAREN
    ;

functionCall
    : IDENTIFIER LPAREN argumentList? RPAREN
    ;

argumentList
    : expression (COMMA expression)*
    ;

ASSIGN   : '=';
PLUS     : '+';
MINUS    : '-';
MULTIPLY : '*';
DIVIDE   : '/';
POWER    : '^';
LPAREN   : '(';
RPAREN   : ')';
COMMA    : ',';

NUMBER
    : [0-9]+ ('.' [0-9]+)?
    ;

IDENTIFIER
    : [a-zA-Z_] [a-zA-Z0-9_]*
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
```

Ela aceita:

``` text
area = pi * r ^ 2
delta = b ^ 2 - four * a * c
x1 = (-b + sqrt(delta)) / (two * a)
hypotenuse = sqrt(a ^ 2 + b ^ 2)
result = sin(angle)
```

Gere o parser:

``` bash
./gradlew generateKotlinGrammarSource
```

No Windows:

``` powershell
.\gradlew.bat generateKotlinGrammarSource
```

O objetivo é encontrar em `build/generatedAntlr/...`:

``` text
FormulaLexer.kt
FormulaParser.kt
FormulaVisitor.kt
FormulaBaseVisitor.kt
```

------------------------------------------------------------------------

# 5. AST

Crie `ast/Ast.kt`:

``` kotlin
package br.com.luizleme.formula.ast

import java.math.BigDecimal

sealed interface Expression

data class NumberLiteral(
    val value: BigDecimal
) : Expression

data class VariableReference(
    val name: String
) : Expression

data class BinaryExpression(
    val left: Expression,
    val operator: BinaryOperator,
    val right: Expression
) : Expression

enum class BinaryOperator {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    POWER
}

data class UnaryExpression(
    val operator: UnaryOperator,
    val expression: Expression
) : Expression

enum class UnaryOperator {
    PLUS,
    MINUS
}

data class FunctionCall(
    val name: String,
    val arguments: List<Expression>
) : Expression

data class Assignment(
    val target: String,
    val expression: Expression
)
```

A Parse Tree responde "como o texto percorreu a gramática?". A AST
responde "o que a expressão significa para o nosso motor?".

``` text
area = pi * r ^ 2

        ↓ AST

Assignment
├── target = area
└── MULTIPLY
    ├── VARIABLE(pi)
    └── POWER
        ├── VARIABLE(r)
        └── NUMBER(2)
```

------------------------------------------------------------------------

# 6. Parser facade

Crie `compiler/FormulaParserFacade.kt`:

``` kotlin
package br.com.luizleme.formula.compiler

import br.com.luizleme.formula.antlr.FormulaLexer
import br.com.luizleme.formula.antlr.FormulaParser
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream

class FormulaParserFacade {

    fun parse(source: String): FormulaParser.AssignmentContext {
        val input = CharStreams.fromString(source)
        val lexer = FormulaLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = FormulaParser(tokens)

        return parser.assignment()
    }
}
```

------------------------------------------------------------------------

# 7. AstBuilder

Crie `compiler/AstBuilder.kt`:

``` kotlin
package br.com.luizleme.formula.compiler

import br.com.luizleme.formula.antlr.FormulaParser
import br.com.luizleme.formula.ast.*
import java.math.BigDecimal

class AstBuilder {

    fun build(ctx: FormulaParser.AssignmentContext): Assignment =
        Assignment(
            target = ctx.IDENTIFIER().text,
            expression = buildExpression(ctx.expression())
        )

    private fun buildExpression(
        ctx: FormulaParser.ExpressionContext
    ): Expression =
        buildAdditive(ctx.additiveExpression())

    private fun buildAdditive(
        ctx: FormulaParser.AdditiveExpressionContext
    ): Expression {
        val operands = ctx.multiplicativeExpression()
        var result = buildMultiplicative(operands.first())

        for (index in 1 until operands.size) {
            val symbol = ctx.getChild(index * 2 - 1).text
            val right = buildMultiplicative(operands[index])

            val operator = when (symbol) {
                "+" -> BinaryOperator.ADD
                "-" -> BinaryOperator.SUBTRACT
                else -> error("Operador aditivo inválido: $symbol")
            }

            result = BinaryExpression(result, operator, right)
        }

        return result
    }

    private fun buildMultiplicative(
        ctx: FormulaParser.MultiplicativeExpressionContext
    ): Expression {
        val operands = ctx.powerExpression()
        var result = buildPower(operands.first())

        for (index in 1 until operands.size) {
            val symbol = ctx.getChild(index * 2 - 1).text
            val right = buildPower(operands[index])

            val operator = when (symbol) {
                "*" -> BinaryOperator.MULTIPLY
                "/" -> BinaryOperator.DIVIDE
                else -> error("Operador multiplicativo inválido: $symbol")
            }

            result = BinaryExpression(result, operator, right)
        }

        return result
    }

    private fun buildPower(
        ctx: FormulaParser.PowerExpressionContext
    ): Expression {
        val left = buildUnary(ctx.unaryExpression())
        val right = ctx.powerExpression()

        return if (right == null) {
            left
        } else {
            BinaryExpression(
                left = left,
                operator = BinaryOperator.POWER,
                right = buildPower(right)
            )
        }
    }

    private fun buildUnary(
        ctx: FormulaParser.UnaryExpressionContext
    ): Expression {
        val nested = ctx.unaryExpression()

        if (nested != null) {
            val operator = when (ctx.getChild(0).text) {
                "+" -> UnaryOperator.PLUS
                "-" -> UnaryOperator.MINUS
                else -> error("Operador unário inválido.")
            }

            return UnaryExpression(
                operator = operator,
                expression = buildUnary(nested)
            )
        }

        return buildPrimary(ctx.primaryExpression())
    }

    private fun buildPrimary(
        ctx: FormulaParser.PrimaryExpressionContext
    ): Expression {
        ctx.NUMBER()?.let {
            return NumberLiteral(BigDecimal(it.text))
        }

        ctx.functionCall()?.let {
            return buildFunctionCall(it)
        }

        ctx.IDENTIFIER()?.let {
            return VariableReference(it.text)
        }

        ctx.expression()?.let {
            return buildExpression(it)
        }

        error("Expressão primária inválida: ${ctx.text}")
    }

    private fun buildFunctionCall(
        ctx: FormulaParser.FunctionCallContext
    ): Expression =
        FunctionCall(
            name = ctx.IDENTIFIER().text,
            arguments =
                ctx.argumentList()
                    ?.expression()
                    ?.map(::buildExpression)
                    ?: emptyList()
        )
}
```

O `AstBuilder` é a fronteira:

``` text
ANTLR / Parse Tree
        ↓
    AstBuilder
        ↓
Nosso domínio / AST
```

------------------------------------------------------------------------

# 8. Modelo de grupo e tipos

Crie `model/CalculationModels.kt`:

``` kotlin
package br.com.luizleme.formula.model

import br.com.luizleme.formula.ast.Assignment
import kotlinx.serialization.Serializable

@Serializable
enum class VariableSource {
    CONSTANT,
    INPUT,
    CALCULATED
}

@Serializable
enum class ValueType {
    INTEGER,
    DECIMAL,
    STRING
}

@Serializable
data class VariableDefinition(
    val name: String,
    val source: VariableSource,
    val type: ValueType,
    val value: String? = null
)

@Serializable
data class FormulaDefinition(
    val name: String,
    val expression: String
)

@Serializable
data class CalculationGroup(
    val id: String,
    val variables: List<VariableDefinition>,
    val formulas: List<FormulaDefinition>,
    val outputs: List<String>
)

@Serializable
data class CalculationRequest(
    val items: List<Map<String, String>>
)

data class CompiledFormula(
    val name: String,
    val source: String,
    val assignment: Assignment,
    val produces: String,
    val requires: Set<String>
)

data class CompiledCalculationGroup(
    val id: String,
    val variables: List<VariableDefinition>,
    val formulas: List<CompiledFormula>,
    val outputs: List<String>
)

data class ItemResult(
    val values: Map<String, RuntimeValue>
)

data class CalculationResult(
    val groupId: String,
    val items: List<ItemResult>
)
```

------------------------------------------------------------------------

# 9. Valores de runtime

Crie `model/RuntimeValue.kt`:

``` kotlin
package br.com.luizleme.formula.model

import java.math.BigDecimal
import java.math.BigInteger

sealed interface RuntimeValue {
    fun type(): ValueType
}

data class IntegerValue(
    val value: BigInteger
) : RuntimeValue {
    override fun type() = ValueType.INTEGER
}

data class DecimalValue(
    val value: BigDecimal
) : RuntimeValue {
    override fun type() = ValueType.DECIMAL
}

data class StringValue(
    val value: String
) : RuntimeValue {
    override fun type() = ValueType.STRING
}

fun parseRuntimeValue(
    type: ValueType,
    rawValue: String
): RuntimeValue =
    when (type) {
        ValueType.INTEGER ->
            IntegerValue(rawValue.toBigInteger())

        ValueType.DECIMAL ->
            DecimalValue(rawValue.toBigDecimal())

        ValueType.STRING ->
            StringValue(rawValue)
    }

fun RuntimeValue.toBigDecimal(): BigDecimal =
    when (this) {
        is DecimalValue -> value
        is IntegerValue -> value.toBigDecimal()

        is StringValue ->
            throw IllegalArgumentException(
                "Texto '$value' não pode participar de operação matemática."
            )
    }
```

Isso evita usar `Any`.

------------------------------------------------------------------------

# 10. DependencyAnalyzer

Crie `analysis/DependencyAnalyzer.kt`:

``` kotlin
package br.com.luizleme.formula.analysis

import br.com.luizleme.formula.ast.*

class DependencyAnalyzer {

    fun findDependencies(
        expression: Expression
    ): Set<String> {
        val result = linkedSetOf<String>()
        collect(expression, result)
        return result
    }

    private fun collect(
        expression: Expression,
        result: MutableSet<String>
    ) {
        when (expression) {
            is NumberLiteral -> Unit

            is VariableReference ->
                result += expression.name

            is BinaryExpression -> {
                collect(expression.left, result)
                collect(expression.right, result)
            }

            is UnaryExpression ->
                collect(expression.expression, result)

            is FunctionCall ->
                expression.arguments.forEach {
                    collect(it, result)
                }
        }
    }
}
```

------------------------------------------------------------------------

# 11. FormulaCompiler

Crie `compiler/FormulaCompiler.kt`:

``` kotlin
package br.com.luizleme.formula.compiler

import br.com.luizleme.formula.analysis.DependencyAnalyzer
import br.com.luizleme.formula.model.CompiledFormula
import br.com.luizleme.formula.model.FormulaDefinition

class FormulaCompiler(
    private val parser: FormulaParserFacade = FormulaParserFacade(),
    private val astBuilder: AstBuilder = AstBuilder(),
    private val dependencyAnalyzer: DependencyAnalyzer = DependencyAnalyzer()
) {

    fun compile(
        definition: FormulaDefinition
    ): CompiledFormula {
        val parseTree = parser.parse(definition.expression)
        val assignment = astBuilder.build(parseTree)

        return CompiledFormula(
            name = definition.name,
            source = definition.expression,
            assignment = assignment,
            produces = assignment.target,
            requires =
                dependencyAnalyzer.findDependencies(
                    assignment.expression
                )
        )
    }
}
```

O `FormulaCompiler` encapsula:

``` text
texto
 ↓
Lexer
 ↓
Parser
 ↓
Parse Tree
 ↓
AstBuilder
 ↓
AST
 ↓
DependencyAnalyzer
 ↓
CompiledFormula
```

------------------------------------------------------------------------

# 12. SemanticAnalyzer

Crie `analysis/SemanticAnalyzer.kt`:

``` kotlin
package br.com.luizleme.formula.analysis

import br.com.luizleme.formula.model.CompiledCalculationGroup
import br.com.luizleme.formula.model.VariableSource

class SemanticAnalyzer {

    fun validate(group: CompiledCalculationGroup) {
        val names = group.variables.map { it.name }
        val uniqueNames = names.toSet()

        require(names.size == uniqueNames.size) {
            "Existem variáveis duplicadas."
        }

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
                "Fórmula '${formula.name}' produz variável não cadastrada '${formula.produces}'."
            }

            formula.requires.forEach {
                require(it in uniqueNames) {
                    "Fórmula '${formula.name}' usa variável desconhecida '$it'."
                }
            }
        }

        group.variables
            .filter { it.source == VariableSource.CALCULATED }
            .forEach {
                require(it.name in producers) {
                    "Variável calculada '${it.name}' não possui fórmula."
                }
            }

        group.outputs.forEach {
            require(it in uniqueNames) {
                "Output '$it' não é uma variável cadastrada."
            }
        }
    }
}
```

------------------------------------------------------------------------

# 13. Compilador do grupo

Crie `compiler/CalculationGroupCompiler.kt`:

``` kotlin
package br.com.luizleme.formula.compiler

import br.com.luizleme.formula.analysis.SemanticAnalyzer
import br.com.luizleme.formula.model.CalculationGroup
import br.com.luizleme.formula.model.CompiledCalculationGroup

class CalculationGroupCompiler(
    private val formulaCompiler: FormulaCompiler = FormulaCompiler(),
    private val semanticAnalyzer: SemanticAnalyzer = SemanticAnalyzer()
) {

    fun compile(
        group: CalculationGroup
    ): CompiledCalculationGroup {
        val compiled =
            CompiledCalculationGroup(
                id = group.id,
                variables = group.variables,
                formulas = group.formulas.map(formulaCompiler::compile),
                outputs = group.outputs
            )

        semanticAnalyzer.validate(compiled)

        return compiled
    }
}
```

------------------------------------------------------------------------

# 14. ExecutionContext

Crie `runtime/ExecutionContext.kt`:

``` kotlin
package br.com.luizleme.formula.runtime

import br.com.luizleme.formula.model.RuntimeValue

class ExecutionContext(
    initialValues: Map<String, RuntimeValue>
) {
    private val values =
        initialValues.toMutableMap()

    fun contains(name: String): Boolean =
        values.containsKey(name)

    fun get(name: String): RuntimeValue =
        values[name]
            ?: throw IllegalArgumentException(
                "Variável '$name' não possui valor no contexto."
            )

    fun set(
        name: String,
        value: RuntimeValue
    ) {
        values[name] = value
    }

    fun names(): Set<String> =
        values.keys.toSet()

    fun snapshot(): Map<String, RuntimeValue> =
        values.toMap()
}
```

------------------------------------------------------------------------

# 15. ExecutionContextFactory

Crie `runtime/ExecutionContextFactory.kt`:

``` kotlin
package br.com.luizleme.formula.runtime

import br.com.luizleme.formula.model.*

class ExecutionContextFactory {

    fun create(
        group: CompiledCalculationGroup,
        input: Map<String, String>
    ): ExecutionContext {
        val initial =
            mutableMapOf<String, RuntimeValue>()

        group.variables
            .filter { it.source == VariableSource.CONSTANT }
            .forEach { variable ->
                val raw =
                    requireNotNull(variable.value) {
                        "Constante '${variable.name}' sem valor."
                    }

                initial[variable.name] =
                    parseRuntimeValue(
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
                    ?: throw IllegalArgumentException(
                        "Input '${variable.name}' não informado."
                    )

            initial[variable.name] =
                parseRuntimeValue(
                    variable.type,
                    raw
                )
        }

        val allowed =
            inputVariables.map { it.name }.toSet()

        val unknown =
            input.keys - allowed

        require(unknown.isEmpty()) {
            "Inputs desconhecidos: $unknown"
        }

        return ExecutionContext(initial)
    }
}
```

------------------------------------------------------------------------

# 16. Funções matemáticas

Crie `function/FormulaFunction.kt`:

``` kotlin
package br.com.luizleme.formula.function

import br.com.luizleme.formula.model.RuntimeValue

interface FormulaFunction {
    val name: String

    fun execute(
        arguments: List<RuntimeValue>
    ): RuntimeValue
}
```

Crie `function/StandardFunctions.kt`:

``` kotlin
package br.com.luizleme.formula.function

import br.com.luizleme.formula.model.DecimalValue
import br.com.luizleme.formula.model.RuntimeValue
import br.com.luizleme.formula.model.toBigDecimal
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

    return arguments.single()
        .toBigDecimal()
        .toDouble()
}

class SqrtFunction : FormulaFunction {
    override val name = "sqrt"

    override fun execute(
        arguments: List<RuntimeValue>
    ): RuntimeValue {
        val value = oneArgument(name, arguments)

        require(value >= 0.0) {
            "sqrt não aceita valor negativo nesta V1."
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
```

------------------------------------------------------------------------

# 17. ExpressionEvaluator

Crie `runtime/ExpressionEvaluator.kt`:

``` kotlin
package br.com.luizleme.formula.runtime

import br.com.luizleme.formula.ast.*
import br.com.luizleme.formula.function.FunctionRegistry
import br.com.luizleme.formula.model.DecimalValue
import br.com.luizleme.formula.model.RuntimeValue
import br.com.luizleme.formula.model.toBigDecimal
import java.math.BigDecimal
import java.math.MathContext

class ExpressionEvaluator(
    private val functions: FunctionRegistry = FunctionRegistry(),
    private val mathContext: MathContext = MathContext.DECIMAL128
) {

    fun evaluate(
        expression: Expression,
        context: ExecutionContext
    ): RuntimeValue =
        when (expression) {
            is NumberLiteral ->
                DecimalValue(expression.value)

            is VariableReference ->
                context.get(expression.name)

            is UnaryExpression ->
                evaluateUnary(expression, context)

            is BinaryExpression ->
                evaluateBinary(expression, context)

            is FunctionCall ->
                functions.execute(
                    expression.name,
                    expression.arguments.map {
                        evaluate(it, context)
                    }
                )
        }

    private fun evaluateUnary(
        expression: UnaryExpression,
        context: ExecutionContext
    ): RuntimeValue {
        val value =
            evaluate(
                expression.expression,
                context
            ).toBigDecimal()

        return when (expression.operator) {
            UnaryOperator.PLUS ->
                DecimalValue(value)

            UnaryOperator.MINUS ->
                DecimalValue(
                    value.negate(mathContext)
                )
        }
    }

    private fun evaluateBinary(
        expression: BinaryExpression,
        context: ExecutionContext
    ): RuntimeValue {
        val left =
            evaluate(
                expression.left,
                context
            ).toBigDecimal()

        val right =
            evaluate(
                expression.right,
                context
            ).toBigDecimal()

        val result =
            when (expression.operator) {
                BinaryOperator.ADD ->
                    left.add(right, mathContext)

                BinaryOperator.SUBTRACT ->
                    left.subtract(right, mathContext)

                BinaryOperator.MULTIPLY ->
                    left.multiply(right, mathContext)

                BinaryOperator.DIVIDE -> {
                    require(
                        right.compareTo(BigDecimal.ZERO) != 0
                    ) {
                        "Divisão por zero."
                    }

                    left.divide(right, mathContext)
                }

                BinaryOperator.POWER -> {
                    val normalized =
                        right.stripTrailingZeros()

                    if (normalized.scale() <= 0) {
                        left.pow(
                            normalized.intValueExact(),
                            mathContext
                        )
                    } else {
                        BigDecimal.valueOf(
                            Math.pow(
                                left.toDouble(),
                                right.toDouble()
                            )
                        )
                    }
                }
            }

        return DecimalValue(result)
    }
}
```

------------------------------------------------------------------------

# 18. ExecutionPlanner

Crie `analysis/ExecutionPlanner.kt`:

``` kotlin
package br.com.luizleme.formula.analysis

import br.com.luizleme.formula.model.CompiledFormula

data class ExecutionPlan(
    val formulas: List<CompiledFormula>
)

class ExecutionPlanner {

    fun plan(
        formulas: List<CompiledFormula>,
        initiallyAvailable: Set<String>
    ): ExecutionPlan {
        val available =
            initiallyAvailable.toMutableSet()

        val pending =
            formulas.toMutableList()

        val ordered =
            mutableListOf<CompiledFormula>()

        while (pending.isNotEmpty()) {
            val executable =
                pending.filter {
                    available.containsAll(it.requires)
                }

            if (executable.isEmpty()) {
                val details =
                    pending.joinToString("\n") {
                        "${it.name}: requires=${it.requires}; available=$available"
                    }

                throw IllegalStateException(
                    "Dependências não resolvidas ou ciclo detectado:\n$details"
                )
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
```

------------------------------------------------------------------------

# 19. Coerção para o tipo cadastrado

Crie `runtime/ValueCoercion.kt`:

``` kotlin
package br.com.luizleme.formula.runtime

import br.com.luizleme.formula.model.*

fun coerceValue(
    value: RuntimeValue,
    targetType: ValueType
): RuntimeValue =
    when (targetType) {
        ValueType.DECIMAL ->
            DecimalValue(
                value.toBigDecimal()
            )

        ValueType.INTEGER ->
            IntegerValue(
                value.toBigDecimal()
                    .toBigIntegerExact()
            )

        ValueType.STRING ->
            when (value) {
                is StringValue -> value
                is IntegerValue ->
                    StringValue(value.value.toString())
                is DecimalValue ->
                    StringValue(value.value.toPlainString())
            }
    }
```

------------------------------------------------------------------------

# 20. CalculationEngine

Crie `runtime/CalculationEngine.kt`:

``` kotlin
package br.com.luizleme.formula.runtime

import br.com.luizleme.formula.analysis.ExecutionPlanner
import br.com.luizleme.formula.compiler.CalculationGroupCompiler
import br.com.luizleme.formula.model.*

class CalculationEngine(
    private val groupCompiler: CalculationGroupCompiler =
        CalculationGroupCompiler(),
    private val contextFactory: ExecutionContextFactory =
        ExecutionContextFactory(),
    private val planner: ExecutionPlanner =
        ExecutionPlanner(),
    private val evaluator: ExpressionEvaluator =
        ExpressionEvaluator()
) {

    fun execute(
        group: CalculationGroup,
        request: CalculationRequest
    ): CalculationResult {
        // O grupo é compilado uma única vez.
        val compiledGroup =
            groupCompiler.compile(group)

        val results =
            request.items.map { input ->
                // Cada item ganha um contexto independente.
                val context =
                    contextFactory.create(
                        compiledGroup,
                        input
                    )

                val plan =
                    planner.plan(
                        formulas =
                            compiledGroup.formulas,
                        initiallyAvailable =
                            context.names()
                    )

                plan.formulas.forEach { formula ->
                    val rawResult =
                        evaluator.evaluate(
                            formula.assignment.expression,
                            context
                        )

                    val target =
                        compiledGroup.variables
                            .first {
                                it.name == formula.produces
                            }

                    val result =
                        coerceValue(
                            rawResult,
                            target.type
                        )

                    context.set(
                        formula.produces,
                        result
                    )
                }

                ItemResult(
                    values =
                        compiledGroup.outputs
                            .associateWith {
                                context.get(it)
                            }
                )
            }

        return CalculationResult(
            groupId = compiledGroup.id,
            items = results
        )
    }
}
```

------------------------------------------------------------------------

# 21. JSON Loader

Crie `serialization/JsonLoader.kt`:

``` kotlin
package br.com.luizleme.formula.serialization

import br.com.luizleme.formula.model.CalculationGroup
import br.com.luizleme.formula.model.CalculationRequest
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
```

------------------------------------------------------------------------

# 22. Impressão do resultado

Crie `serialization/ResultPrinter.kt`:

``` kotlin
package br.com.luizleme.formula.serialization

import br.com.luizleme.formula.model.*

class ResultPrinter {

    fun print(result: CalculationResult) {
        println("Grupo: ${result.groupId}")

        result.items.forEachIndexed { index, item ->
            println("Item ${index + 1}")

            item.values.forEach { (name, value) ->
                println("  $name = ${format(value)}")
            }
        }
    }

    private fun format(value: RuntimeValue): String =
        when (value) {
            is DecimalValue ->
                value.value
                    .stripTrailingZeros()
                    .toPlainString()

            is IntegerValue ->
                value.value.toString()

            is StringValue ->
                value.value
        }
}
```

------------------------------------------------------------------------

# 23. Main

Crie `Main.kt`:

``` kotlin
package br.com.luizleme.formula

import br.com.luizleme.formula.runtime.CalculationEngine
import br.com.luizleme.formula.serialization.JsonLoader
import br.com.luizleme.formula.serialization.ResultPrinter
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
```

------------------------------------------------------------------------

# 24. Exemplo completo --- área do círculo

Crie `examples/circle-group.json`:

``` json
{
  "id": "circle-area",
  "variables": [
    {
      "name": "pi",
      "source": "CONSTANT",
      "type": "DECIMAL",
      "value": "3.14"
    },
    {
      "name": "r",
      "source": "INPUT",
      "type": "DECIMAL"
    },
    {
      "name": "area",
      "source": "CALCULATED",
      "type": "DECIMAL"
    }
  ],
  "formulas": [
    {
      "name": "calculate-area",
      "expression": "area = pi * r ^ 2"
    }
  ],
  "outputs": [
    "area"
  ]
}
```

Crie `examples/circle-request.json`:

``` json
{
  "items": [
    {
      "r": "2"
    },
    {
      "r": "5"
    },
    {
      "r": "10"
    }
  ]
}
```

Execute:

``` bash
./gradlew run --args="examples/circle-group.json examples/circle-request.json"
```

Esperado:

``` text
Grupo: circle-area
Item 1
  area = 12.56
Item 2
  area = 78.5
Item 3
  area = 314
```

------------------------------------------------------------------------

# 25. Passo a passo do contexto do círculo

O grupo é compilado **uma vez**.

Para o primeiro item:

``` json
{
  "r": "2"
}
```

o `ExecutionContextFactory` carrega a constante:

``` text
pi = 3.14
```

e o input:

``` text
r = 2
```

Contexto inicial:

``` text
ExecutionContext
├── pi = 3.14
└── r  = 2
```

A fórmula compilada possui:

``` text
produces = area
requires = pi, r
```

Como `pi` e `r` estão disponíveis, ela pode rodar.

AST:

``` text
MULTIPLY
├── pi
└── POWER
    ├── r
    └── 2
```

Avaliação:

``` text
r → 2
2 ^ 2 → 4
pi → 3.14
3.14 * 4 → 12.56
```

Contexto depois da fórmula:

``` text
ExecutionContext
├── pi   = 3.14
├── r    = 2
└── area = 12.56
```

`area` é output, então sai na resposta.

O segundo e o terceiro círculo recebem **novos contextos**, mas
reutilizam o mesmo grupo compilado.

------------------------------------------------------------------------

# 26. Exemplo completo --- equação quadrática

Crie `examples/quadratic-group.json`:

``` json
{
  "id": "quadratic",
  "variables": [
    {
      "name": "two",
      "source": "CONSTANT",
      "type": "INTEGER",
      "value": "2"
    },
    {
      "name": "four",
      "source": "CONSTANT",
      "type": "INTEGER",
      "value": "4"
    },
    {
      "name": "a",
      "source": "INPUT",
      "type": "DECIMAL"
    },
    {
      "name": "b",
      "source": "INPUT",
      "type": "DECIMAL"
    },
    {
      "name": "c",
      "source": "INPUT",
      "type": "DECIMAL"
    },
    {
      "name": "delta",
      "source": "CALCULATED",
      "type": "DECIMAL"
    },
    {
      "name": "x1",
      "source": "CALCULATED",
      "type": "DECIMAL"
    },
    {
      "name": "x2",
      "source": "CALCULATED",
      "type": "DECIMAL"
    }
  ],
  "formulas": [
    {
      "name": "calculate-delta",
      "expression": "delta = b ^ 2 - four * a * c"
    },
    {
      "name": "calculate-x1",
      "expression": "x1 = (-b + sqrt(delta)) / (two * a)"
    },
    {
      "name": "calculate-x2",
      "expression": "x2 = (-b - sqrt(delta)) / (two * a)"
    }
  ],
  "outputs": [
    "delta",
    "x1",
    "x2"
  ]
}
```

Crie `examples/quadratic-request.json`:

``` json
{
  "items": [
    {
      "a": "1",
      "b": "-5",
      "c": "6"
    }
  ]
}
```

Execute:

``` bash
./gradlew run --args="examples/quadratic-group.json examples/quadratic-request.json"
```

Esperado:

``` text
Grupo: quadratic
Item 1
  delta = 1
  x1 = 3
  x2 = 2
```

------------------------------------------------------------------------

# 27. Passo a passo do contexto de Bhaskara

Contexto inicial:

``` text
two  = 2     CONSTANT
four = 4     CONSTANT
a    = 1     INPUT
b    = -5    INPUT
c    = 6     INPUT
```

O compiler descobriu:

``` text
delta
produces = delta
requires = b, four, a, c
```

``` text
x1
produces = x1
requires = b, delta, two, a
```

``` text
x2
produces = x2
requires = b, delta, two, a
```

No início, `delta` pode rodar. `x1` e `x2` ainda não podem porque
`delta` não existe.

Depois de `delta`:

``` text
two   = 2
four  = 4
a     = 1
b     = -5
c     = 6
delta = 1
```

Agora `x1` e `x2` ficam executáveis.

Depois de `x1`:

``` text
two   = 2
four  = 4
a     = 1
b     = -5
c     = 6
delta = 1
x1    = 3
```

Depois de `x2`:

``` text
two   = 2
four  = 4
a     = 1
b     = -5
c     = 6
delta = 1
x1    = 3
x2    = 2
```

O planner descobriu a ordem. O JSON não precisou trazer `order`.

------------------------------------------------------------------------

# 28. Testes

Crie `src/test/kotlin/br/com/luizleme/formula/CalculationEngineTest.kt`:

``` kotlin
package br.com.luizleme.formula

import br.com.luizleme.formula.model.*
import br.com.luizleme.formula.runtime.CalculationEngine
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculationEngineTest {

    @Test
    fun `calcula area do circulo`() {
        val group =
            CalculationGroup(
                id = "circle",
                variables =
                    listOf(
                        VariableDefinition(
                            "pi",
                            VariableSource.CONSTANT,
                            ValueType.DECIMAL,
                            "3.14"
                        ),
                        VariableDefinition(
                            "r",
                            VariableSource.INPUT,
                            ValueType.DECIMAL
                        ),
                        VariableDefinition(
                            "area",
                            VariableSource.CALCULATED,
                            ValueType.DECIMAL
                        )
                    ),
                formulas =
                    listOf(
                        FormulaDefinition(
                            "area",
                            "area = pi * r ^ 2"
                        )
                    ),
                outputs =
                    listOf("area")
            )

        val request =
            CalculationRequest(
                items =
                    listOf(
                        mapOf("r" to "2")
                    )
            )

        val result =
            CalculationEngine()
                .execute(group, request)

        val area =
            result.items
                .single()
                .values
                .getValue("area")
                as DecimalValue

        assertEquals(
            "12.56",
            area.value
                .stripTrailingZeros()
                .toPlainString()
        )
    }

    @Test
    fun `planner resolve formulas fora de ordem`() {
        val group =
            CalculationGroup(
                id = "dependencies",
                variables =
                    listOf(
                        VariableDefinition(
                            "a",
                            VariableSource.INPUT,
                            ValueType.DECIMAL
                        ),
                        VariableDefinition(
                            "b",
                            VariableSource.CALCULATED,
                            ValueType.DECIMAL
                        ),
                        VariableDefinition(
                            "c",
                            VariableSource.CALCULATED,
                            ValueType.DECIMAL
                        )
                    ),
                formulas =
                    listOf(
                        // Propositalmente c aparece antes de b.
                        FormulaDefinition(
                            "calculate-c",
                            "c = b * 2"
                        ),
                        FormulaDefinition(
                            "calculate-b",
                            "b = a + 10"
                        )
                    ),
                outputs =
                    listOf("b", "c")
            )

        val result =
            CalculationEngine()
                .execute(
                    group,
                    CalculationRequest(
                        listOf(
                            mapOf("a" to "5")
                        )
                    )
                )

        val item =
            result.items.single()

        val b =
            item.values.getValue("b")
                as DecimalValue

        val c =
            item.values.getValue("c")
                as DecimalValue

        assertEquals(
            "15",
            b.value.stripTrailingZeros().toPlainString()
        )

        assertEquals(
            "30",
            c.value.stripTrailingZeros().toPlainString()
        )
    }
}
```

Execute:

``` bash
./gradlew test
```

------------------------------------------------------------------------

# 29. Sobre `STRING`

O modelo já suporta:

``` json
{
  "name": "cardBrand",
  "source": "INPUT",
  "type": "STRING"
}
```

e:

``` json
{
  "name": "country",
  "source": "CONSTANT",
  "type": "STRING",
  "value": "BR"
}
```

Mas a **linguagem V1 é matemática**. Portanto uma `StringValue` não pode
participar de:

``` text
+
-
*
/
^
```

A próxima versão poderá acrescentar à gramática:

``` text
STRING_LITERAL
BOOLEAN
==
!=
>
<
>=
<=
and
or
if
```

A estrutura de tipos criada agora não precisará ser descartada.

------------------------------------------------------------------------

# 30. O que cada peça faz

  Peça                    Responsabilidade
  ----------------------- ---------------------------------------
  `Formula.g4`            Define a linguagem
  `FormulaLexer.kt`       Caracteres → tokens
  `FormulaParser.kt`      Tokens → Parse Tree
  `AstBuilder`            Parse Tree → AST
  AST                     Representação semântica da expressão
  `FormulaCompiler`       Orquestra parsing, AST e dependências
  `SemanticAnalyzer`      Valida o significado do grupo
  `DependencyAnalyzer`    Descobre `requires` e `produces`
  `ExecutionPlanner`      Descobre ordem de execução
  `ExecutionContext`      Guarda valores de uma execução
  `FunctionRegistry`      Implementa `sqrt`, `sin`, `cos` etc.
  `ExpressionEvaluator`   Executa a AST
  `CalculationEngine`     Orquestra a execução do grupo

------------------------------------------------------------------------

# 31. Por que não usar diretamente a Parse Tree?

Poderíamos fazer:

``` text
Parse Tree
   ↓
Evaluator
```

Para uma calculadora didática isso seria suficiente.

Para este projeto queremos:

``` text
Parse Tree
   ↓
AstBuilder
   ↓
AST própria
   ↓
análise
   ↓
execução
```

Porque depois vamos adicionar:

``` text
tipagem
dependências
condicionais
funções de negócio
validação semântica
versionamento
persistência
possível IR própria
```

O núcleo do motor não deve depender das classes `...Context` geradas
pelo ANTLR.

------------------------------------------------------------------------

# 32. Por que existe `FormulaCompiler`?

Sem ele, `CalculationEngine` teria que conhecer:

``` text
CharStreams
FormulaLexer
CommonTokenStream
FormulaParser
Parse Tree
AstBuilder
```

Com ele:

``` kotlin
val compiled =
    formulaCompiler.compile(definition)
```

O motor recebe uma `CompiledFormula`.

Isso cria uma fronteira limpa entre:

``` text
LINGUAGEM
```

e:

``` text
RUNTIME
```

Além disso, um grupo pode ser compilado uma vez e usado para milhares ou
milhões de itens.

Não queremos rodar ANTLR novamente para cada círculo.

------------------------------------------------------------------------

# 33. Ordem de implementação para estudar de verdade

Mesmo com todo o código deste documento disponível, implemente nesta
ordem:

``` text
1. Projeto Kotlin vazio
2. Plugin ANTLR Kotlin
3. Formula.g4
4. generateKotlinGrammarSource
5. conferir FormulaLexer.kt
6. conferir FormulaParser.kt
7. FormulaParserFacade
8. testar Parse Tree
9. Ast.kt
10. AstBuilder
11. imprimir AST de "area = pi * r ^ 2"
12. FormulaCompiler
13. VariableSource e ValueType
14. RuntimeValue
15. ExecutionContext
16. ExpressionEvaluator
17. executar área do círculo sem JSON
18. DependencyAnalyzer
19. ExecutionPlanner
20. executar dependência delta → x1/x2
21. CalculationGroup
22. JSON
23. execução em lote
24. testes
```

A primeira meta concreta é:

``` text
area = pi * r ^ 2
```

chegar corretamente a:

``` text
Assignment
├── area
└── MULTIPLY
    ├── pi
    └── POWER
        ├── r
        └── 2
```

A segunda é executar essa AST.

A terceira é fazer Bhaskara provar que o mecanismo de dependências
funciona.

------------------------------------------------------------------------

# 34. Arquitetura final da V1

``` text
                       Formula.g4
                           │
                           ▼
                    ANTLR Kotlin
                           │
               ┌───────────┴───────────┐
               ▼                       ▼
        FormulaLexer.kt         FormulaParser.kt
               │                       │
               └───────────┬───────────┘
                           ▼
                       Parse Tree
                           │
                           ▼
                       AstBuilder
                           │
                           ▼
                          AST
                           │
                           ▼
                    FormulaCompiler
                           │
                           ▼
                    CompiledFormula
                           │
                           ▼
                 DependencyAnalyzer
                           │
                           ▼
               CompiledCalculationGroup
                           │
                           ▼
                   SemanticAnalyzer
                           │
                           ▼
                    CalculationEngine
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
         Constants                   Request
              │                         │
              └────────────┬────────────┘
                           ▼
                   ExecutionContext
                           │
                           ▼
                   ExecutionPlanner
                           │
                           ▼
                  ExpressionEvaluator
                           │
                ┌──────────┴──────────┐
                ▼                     ▼
             Operators          FunctionRegistry
             + - * / ^          sqrt sin cos...
                │                     │
                └──────────┬──────────┘
                           ▼
                  ExecutionContext
                     atualizado
                           │
                           ▼
                        Outputs
```

------------------------------------------------------------------------

# 35. O princípio que queremos preservar

Adicionar uma nova fórmula deve significar:

``` text
CADASTRAR DADOS
```

e não:

``` text
ALTERAR CalculationEngine
```

Portanto:

``` text
area = pi * r ^ 2
force = mass * acceleration
velocity = distance / time
hypotenuse = sqrt(a ^ 2 + b ^ 2)
delta = b ^ 2 - four * a * c
```

devem atravessar exatamente o mesmo pipeline.

Esse é o núcleo do protótipo que depois poderá receber Spring Boot,
persistência, API e uma tela Thymeleaf para cadastrar grupos, variáveis,
constantes e fórmulas.
