<div align="center">

<img src="./logo-dsl-calculos.png" alt="DSL Cálculos" width="350">

# DSL de Cálculos Matemáticos e Físicos

</div>

> Uma Domain-Specific Language (DSL) para definição, validação e execução de fórmulas matemáticas e físicas, construída com **Kotlin** e **ANTLR**.

## 📌 Sobre o projeto

Este projeto tem como objetivo desenvolver uma **linguagem específica de domínio para cálculos**, permitindo representar fórmulas matemáticas e físicas através de uma sintaxe própria, independente da linguagem utilizada pela aplicação cliente.

A proposta vai além de uma calculadora convencional.

Em vez de implementar cada cálculo diretamente no código da aplicação, o projeto cria uma linguagem capaz de:

* representar fórmulas;
* interpretar expressões;
* validar sintaxe e semântica;
* construir uma Abstract Syntax Tree (AST);
* resolver variáveis e constantes;
* executar cálculos;
* retornar resultados estruturados;
* permitir a criação de novas fórmulas sem alterar o núcleo do motor.

O projeto também funciona como estudo prático de:

* construção de linguagens;
* compiladores e interpretadores;
* análise léxica;
* análise sintática;
* gramáticas formais;
* AST;
* validação semântica;
* arquitetura de software;
* Domain-Specific Languages.

---

# 🎯 Objetivo

Construir um **motor genérico de cálculos baseado em DSL** capaz de receber expressões matemáticas ou físicas, interpretar sua estrutura e executá-las utilizando valores fornecidos em tempo de execução.

Exemplo simples:

```text
resultado = (a + b) * c
```

Com:

```text
a = 10
b = 5
c = 2
```

Resultado:

```text
resultado = 30
```

Entretanto, o objetivo do projeto é evoluir para fórmulas significativamente mais complexas.

Por exemplo:

```text
delta = b^2 - 4 * a * c
x1 = (-b + sqrt(delta)) / (2 * a)
x2 = (-b - sqrt(delta)) / (2 * a)
```

Ou fórmulas físicas:

```text
velocidadeMedia = distancia / tempo
```

```text
forca = massa * aceleracao
```

```text
energiaCinetica = (massa * velocidade^2) / 2
```

```text
energiaPotencial = massa * gravidade * altura
```

A DSL deverá permitir que novas fórmulas sejam adicionadas progressivamente sem transformar o motor em um conjunto de implementações específicas para cada cálculo.

---

# 🧠 Conceito central

O fluxo principal da DSL será:

```text
             Fórmula
                │
                ▼
        ┌───────────────┐
        │     Lexer     │
        │     ANTLR     │
        └───────┬───────┘
                │
              Tokens
                │
                ▼
        ┌───────────────┐
        │    Parser     │
        │     ANTLR     │
        └───────┬───────┘
                │
          Parse Tree
                │
                ▼
        ┌───────────────┐
        │  AST Builder  │
        └───────┬───────┘
                │
               AST
                │
                ▼
        ┌───────────────┐
        │   Validator   │
        └───────┬───────┘
                │
                ▼
        ┌───────────────┐
        │ Calculation   │
        │    Engine     │
        └───────┬───────┘
                │
                ▼
        ┌───────────────┐
        │    Result     │
        └───────────────┘
```

De forma resumida:

```text
Formula
   ↓
Lexer
   ↓
Tokens
   ↓
Parser
   ↓
Parse Tree
   ↓
AST
   ↓
Semantic Validation
   ↓
Variable Resolution
   ↓
Calculation Engine
   ↓
Result
```

---

# 🏗️ Arquitetura inicial

Uma possível organização do projeto:

```text
dsl-calculations/
│
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
│
├── docs/
│   ├── architecture/
│   ├── grammar/
│   ├── examples/
│   └── decisions/
│
└── src/
    ├── main/
    │   ├── antlr/
    │   │   └── Calculation.g4
    │   │
    │   └── kotlin/
    │       └── dsl/
    │           ├── lexer/
    │           ├── parser/
    │           ├── ast/
    │           ├── validator/
    │           ├── engine/
    │           ├── context/
    │           ├── function/
    │           ├── result/
    │           └── exception/
    │
    └── test/
        └── kotlin/
            └── dsl/
                ├── parser/
                ├── ast/
                ├── validator/
                └── engine/
```

A estrutura deverá evoluir conforme novas necessidades forem descobertas durante o desenvolvimento.

---

# 🔤 Gramática

A gramática será definida utilizando **ANTLR**.

Exemplo inicial simplificado:

```antlr
grammar Calculation;

expression
    : expression '^' expression
    | expression ('*' | '/') expression
    | expression ('+' | '-') expression
    | function
    | NUMBER
    | IDENTIFIER
    | '(' expression ')'
    ;

function
    : IDENTIFIER '(' expression ')'
    ;

NUMBER
    : [0-9]+ ('.' [0-9]+)?
    ;

IDENTIFIER
    : [a-zA-Z_][a-zA-Z0-9_]*
    ;

WS
    : [ \t\r\n]+ -> skip
    ;
```

Esta gramática é apenas o ponto de partida.

Ela deverá evoluir para suportar:

* precedência de operadores;
* associatividade;
* operadores unários;
* potência;
* funções matemáticas;
* constantes;
* múltiplas expressões;
* atribuições;
* tipos;
* validações;
* unidades físicas.

---

# 🌳 Abstract Syntax Tree — AST

Após o parsing, a árvore gerada pelo ANTLR não será utilizada diretamente pelo domínio.

O projeto deverá possuir sua própria representação de AST.

Exemplo conceitual:

```kotlin
sealed interface Expression
```

Implementações possíveis:

```text
Expression
│
├── LiteralExpression
├── VariableExpression
├── BinaryExpression
├── UnaryExpression
├── FunctionExpression
└── AssignmentExpression
```

Uma expressão como:

```text
(a + b) * c
```

poderá resultar conceitualmente em:

```text
        *
       / \
      +   c
     / \
    a   b
```

Isso mantém o domínio independente das estruturas internas geradas pelo ANTLR.

---

# 🔎 Validação

Antes da execução, as fórmulas deverão passar por validações.

Existirão pelo menos dois níveis.

## Validação sintática

Responsabilidade principalmente do parser.

Exemplo inválido:

```text
10 + * 20
```

## Validação semântica

A expressão pode estar sintaticamente correta, mas semanticamente inválida.

Exemplo:

```text
resultado = massa * variavelInexistente
```

Possíveis validações:

* variável não definida;
* função inexistente;
* quantidade incorreta de argumentos;
* tipos incompatíveis;
* divisão por zero;
* dependência circular;
* constantes inválidas;
* domínio inválido de função.

Exemplo:

```text
sqrt(-10)
```

Dependendo das regras configuradas para o domínio numérico, essa expressão poderá gerar um erro semântico.

---

# ⚙️ Motor de execução

O `CalculationEngine` será responsável por avaliar a AST.

Exemplo:

```text
massa = 10
aceleracao = 5
```

Fórmula:

```text
forca = massa * aceleracao
```

Execução conceitual:

```text
Variable(massa)
        ↓
       10

Variable(aceleracao)
        ↓
        5

BinaryExpression(*)
        ↓
     10 * 5
        ↓
       50
```

Resultado:

```json
{
  "variable": "forca",
  "value": 50
}
```

---

# 📦 Contexto de execução

As fórmulas não deverão conhecer diretamente banco de dados, APIs ou interfaces externas.

O motor receberá um contexto contendo os valores necessários para execução.

Exemplo:

```json
{
  "variables": {
    "massa": 10,
    "aceleracao": 9.81
  }
}
```

Fórmula:

```text
forca = massa * aceleracao
```

Resultado:

```json
{
  "result": {
    "forca": 98.1
  }
}
```

Isso permite que os valores tenham diferentes origens:

```text
HTTP Request
Database
Configuration
External API
Previous Calculation
        │
        ▼
Execution Context
        │
        ▼
Calculation Engine
```

---

# 🔗 Cálculos encadeados

Um dos objetivos importantes da DSL é permitir cálculos dependentes de resultados anteriores.

Exemplo:

```text
delta = b^2 - 4 * a * c
x1 = (-b + sqrt(delta)) / (2 * a)
x2 = (-b - sqrt(delta)) / (2 * a)
```

Nesse caso existe um grafo de dependências:

```text
a ─────┐
b ─────┼────► delta
c ─────┘        │
                ├────► x1
                │
                └────► x2
```

O motor deverá identificar essas dependências e determinar a ordem correta de execução.

Também deverá detectar dependências circulares.

Exemplo inválido:

```text
a = b + 1
b = a + 1
```

---

# 🧮 Operações matemáticas

A primeira evolução da DSL deverá oferecer suporte aos operadores fundamentais:

```text
+
-
*
/
%
^
```

Também deverão ser suportadas funções matemáticas como:

```text
sqrt(x)
abs(x)
pow(x, y)
min(x, y)
max(x, y)
round(x)
```

Posteriormente:

```text
sin(x)
cos(x)
tan(x)
log(x)
ln(x)
exp(x)
```

---

# 🔢 Constantes

A linguagem poderá possuir constantes matemáticas e físicas.

Exemplos matemáticos:

```text
PI
E
```

Exemplos físicos:

```text
GRAVITY
SPEED_OF_LIGHT
```

Exemplo:

```text
circunferencia = 2 * PI * raio
```

---

# 🔬 Fórmulas físicas

A arquitetura deverá permitir representar diferentes categorias de cálculos físicos.

## Cinemática

```text
velocidadeMedia = distancia / tempo
```

```text
movimentoUniforme = posicaoInicial + velocidade * tempo
```

## Dinâmica

```text
forca = massa * aceleracao
```

## Energia

```text
energiaCinetica = massa * velocidade^2 / 2
```

```text
energiaPotencial = massa * gravidade * altura
```

## Eletricidade

```text
tensao = resistencia * corrente
```

## Densidade

```text
densidade = massa / volume
```

A intenção não é implementar cada equação como código Kotlin.

As fórmulas deverão ser representadas, sempre que possível, **pela própria DSL**.

---

# 📐 Unidades de medida

Uma evolução importante do projeto será adicionar suporte a grandezas e unidades.

Exemplo:

```text
massa = 10 kg
aceleracao = 9.81 m/s^2

forca = massa * aceleracao
```

Resultado esperado:

```text
98.1 N
```

Isso exigirá futuramente conceitos como:

```text
Value
│
├── NumericValue
│
└── Quantity
     ├── value
     ├── unit
     └── dimension
```

O motor poderá validar operações dimensionalmente incompatíveis.

Por exemplo:

```text
10 kg + 20 m
```

deverá ser rejeitado.

---

# 📚 Biblioteca de fórmulas

No futuro, fórmulas poderão ser organizadas em bibliotecas.

Exemplo:

```text
formulas/
│
├── mathematics/
│   ├── arithmetic
│   ├── algebra
│   ├── geometry
│   └── trigonometry
│
└── physics/
    ├── mechanics
    ├── kinematics
    ├── dynamics
    ├── thermodynamics
    └── electricity
```

Isso permitirá reutilizar o mesmo motor para diferentes domínios.

---

# 🧩 Exemplo completo

Entrada:

```json
{
  "formula": "energiaCinetica = massa * velocidade^2 / 2",
  "variables": {
    "massa": 80,
    "velocidade": 10
  }
}
```

Processamento:

```text
JSON
 │
 ▼
Formula
 │
 ▼
ANTLR Lexer
 │
 ▼
ANTLR Parser
 │
 ▼
Parse Tree
 │
 ▼
AST Builder
 │
 ▼
AST
 │
 ▼
Semantic Validator
 │
 ▼
Variable Resolver
 │
 ▼
Calculation Engine
 │
 ▼
Result
```

Resultado:

```json
{
  "formula": "energiaCinetica",
  "value": 4000
}
```

---

# 🧱 Separação de responsabilidades

## Grammar

Define **como a linguagem pode ser escrita**.

```text
Calculation.g4
```

## Lexer

Transforma caracteres em tokens.

```text
"10 + massa"
```

torna-se conceitualmente:

```text
NUMBER(10)
PLUS
IDENTIFIER(massa)
```

## Parser

Valida a estrutura sintática e produz a Parse Tree.

## AST Builder

Converte estruturas do ANTLR para objetos pertencentes ao domínio da DSL.

## Validator

Executa validações semânticas.

## Context

Mantém variáveis, constantes e demais valores necessários para uma execução.

## Engine

Percorre a AST e realiza os cálculos.

## Function Registry

Mantém as funções disponíveis na linguagem.

Exemplo:

```text
sqrt
sin
cos
pow
abs
```

## Result

Representa o resultado da execução de forma independente da interface externa.

---

# 🏛️ Princípios arquiteturais

O projeto seguirá alguns princípios importantes.

### Independência do ANTLR

ANTLR será utilizado para parsing, mas não deverá contaminar todo o domínio.

```text
ANTLR
  │
  ▼
Adapter / AST Builder
  │
  ▼
Domain AST
```

### Motor independente de transporte

O motor não deverá depender de:

```text
REST
HTTP
JSON
Database
Angular
Spring
```

Essas tecnologias poderão consumir o motor, mas não definir seu funcionamento.

### Fórmulas como dados

Sempre que possível:

```text
Formula != Kotlin Code
```

A fórmula deve ser armazenável, versionável, validável e executável pela DSL.

### Extensibilidade

Adicionar uma nova fórmula idealmente não deverá exigir alterações no núcleo do motor.

---

# 🧪 Estratégia de testes

O projeto deverá possuir testes em diferentes níveis.

## Grammar Tests

```text
"1 + 2"
"a * b"
"(a + b) * c"
```

## Parser Tests

Verificar se expressões válidas e inválidas são reconhecidas corretamente.

## AST Tests

```text
1 + 2 * 3
```

deve respeitar precedência:

```text
    +
   / \
  1   *
     / \
    2   3
```

## Validator Tests

Testar:

* variáveis inexistentes;
* funções inexistentes;
* argumentos inválidos;
* dependências circulares;
* tipos incompatíveis.

## Engine Tests

```text
2 + 2 = 4
```

```text
2 + 3 * 4 = 14
```

```text
(2 + 3) * 4 = 20
```

## Formula Tests

Exemplo:

```text
massa = 80
velocidade = 10

energiaCinetica = massa * velocidade^2 / 2
```

Resultado:

```text
4000
```

---

# 🗺️ Roadmap

## Fase 1 — Calculadora básica

* [ ] Criar projeto Kotlin
* [ ] Configurar ANTLR
* [ ] Criar gramática inicial
* [ ] Implementar soma
* [ ] Implementar subtração
* [ ] Implementar multiplicação
* [ ] Implementar divisão
* [ ] Implementar parênteses
* [ ] Implementar precedência

## Fase 2 — AST

* [ ] Criar modelo da AST
* [ ] Criar AST Builder
* [ ] Remover dependência direta da Parse Tree no domínio
* [ ] Criar Visitor/Evaluator

## Fase 3 — Variáveis

* [ ] Implementar identificadores
* [ ] Implementar atribuições
* [ ] Criar Execution Context
* [ ] Criar Variable Resolver
* [ ] Suportar resultados intermediários

## Fase 4 — Funções matemáticas

* [ ] `sqrt`
* [ ] `pow`
* [ ] `abs`
* [ ] `min`
* [ ] `max`
* [ ] `sin`
* [ ] `cos`
* [ ] `tan`
* [ ] `log`

## Fase 5 — Validação semântica

* [ ] Variáveis inexistentes
* [ ] Funções inexistentes
* [ ] Quantidade de argumentos
* [ ] Divisão por zero
* [ ] Dependências circulares
* [ ] Domínio das funções

## Fase 6 — Fórmulas encadeadas

* [ ] Criar grafo de dependências
* [ ] Ordenação das fórmulas
* [ ] Resultados intermediários
* [ ] Detecção de ciclos

## Fase 7 — Física

* [ ] Constantes físicas
* [ ] Grandezas
* [ ] Unidades
* [ ] Dimensões
* [ ] Conversão de unidades
* [ ] Validação dimensional

## Fase 8 — Biblioteca de fórmulas

* [ ] Matemática
* [ ] Álgebra
* [ ] Geometria
* [ ] Trigonometria
* [ ] Cinemática
* [ ] Dinâmica
* [ ] Energia
* [ ] Eletricidade

## Fase 9 — Persistência e API

* [ ] Representação JSON das fórmulas
* [ ] Versionamento
* [ ] Persistência
* [ ] API para validação
* [ ] API para execução

---

# 🚀 Visão futura

A arquitetura poderá evoluir para algo semelhante a:

```text
             ┌─────────────────┐
             │ Formula Editor  │
             │     Angular     │
             └────────┬────────┘
                      │
                     JSON
                      │
                      ▼
             ┌─────────────────┐
             │   Formula API   │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │   DSL Compiler  │
             │                 │
             │ Lexer           │
             │ Parser          │
             │ AST Builder     │
             │ Validator       │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ Calculation     │
             │ Engine          │
             └────────┬────────┘
                      │
             ┌────────┴────────┐
             ▼                 ▼
       Formula Store        Result
```

A interface poderá permitir futuramente a construção visual das fórmulas, enquanto o backend permanece responsável pela validação e execução.

---

# 🛠️ Tecnologias

Inicialmente:

* **Kotlin**
* **ANTLR 4**
* **Gradle Kotlin DSL**
* **JUnit 5**
* **Git**

Possíveis tecnologias futuras:

* Spring Boot
* PostgreSQL
* Angular
* Docker
* Testcontainers

Essas tecnologias não fazem parte obrigatoriamente do núcleo da DSL.

---

# 📖 Conceitos estudados

Este projeto também será utilizado como laboratório para estudo de:

```text
Compiler Design
Domain-Specific Languages
Formal Grammars
Lexer
Parser
Parse Tree
Abstract Syntax Tree
Visitor Pattern
Interpreter Pattern
Semantic Analysis
Symbol Tables
Dependency Graphs
Type Systems
Dimensional Analysis
Language Engineering
Software Architecture
```

---

# 📚 Referências de estudo

O desenvolvimento será apoiado principalmente em literatura sobre construção de linguagens e DSLs.

### Terence Parr

**The Definitive ANTLR 4 Reference**

Referência principal para ANTLR, gramáticas, parsers, listeners e visitors.

### Terence Parr

**Language Implementation Patterns**

Aborda padrões utilizados na implementação de linguagens, interpretadores e tradutores.

### Martin Fowler

**Domain-Specific Languages**

Referência importante para compreender DSLs internas e externas, parsers, modelos semânticos e estratégias de implementação.

---

# 💡 Filosofia do projeto

Este repositório não pretende apenas responder:

> "Como calcular uma fórmula?"

A pergunta principal é:

> **"Como projetar uma linguagem capaz de representar, validar e executar diferentes tipos de cálculos?"**

Por isso, o projeto começa pequeno:

```text
1 + 2
```

e deverá evoluir progressivamente para:

```text
DSL
 │
 ├── Grammar
 ├── Parser
 ├── AST
 ├── Semantic Model
 ├── Validation
 ├── Functions
 ├── Variables
 ├── Dependency Graph
 ├── Calculation Engine
 ├── Mathematical Formulas
 └── Physical Formulas
```

O objetivo final é possuir um **motor extensível de fórmulas matemáticas e físicas**, no qual a linguagem descreve **o que deve ser calculado**, enquanto o motor determina **como interpretar, validar e executar o cálculo**.

---

## 📄 Licença

Projeto desenvolvido inicialmente para fins de estudo, experimentação e evolução de conhecimentos em engenharia de software, construção de linguagens, compiladores, Kotlin e ANTLR.
