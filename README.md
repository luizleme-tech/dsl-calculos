<div align="center">

<img src="./logo-dsl-calculos.png" alt="DSL Cálculos" width="350">

# 🧮 DSL de Cálculos Matemáticos

</div>

DSL para interpretação e execução de expressões matemáticas desenvolvida em **Kotlin** utilizando **ANTLR 4**.

O projeto implementa um motor de expressões matemáticas para estudar, na prática, a construção de uma linguagem:

```text
Fórmula
   ↓
Lexer
   ↓
Parser
   ↓
Parse Tree
   ↓
AST
   ↓
Evaluator
   ↓
Resultado
```

---

## ✅ Funcionalidades implementadas

Atualmente o Formula Engine possui suporte a:

* números inteiros;
* números decimais;
* variáveis;
* adição (`+`);
* subtração (`-`);
* multiplicação (`*`);
* divisão (`/`);
* potenciação (`^`);
* operadores unários `+` e `-`;
* parênteses;
* precedência de operadores;
* construção de AST;
* avaliação das expressões;
* tratamento de erros;
* testes automatizados.

Exemplos de expressões suportadas:

```text
2 + 3
10 - 4
5 * 8
20 / 4
2 ^ 3
2 + 3 * 4
(2 + 3) * 4
-10
-x
base * altura
```

---

# 🏗️ Fluxo de execução

Uma expressão como:

```text
2 + 3 * 4
```

passa pelo seguinte fluxo:

```text
"2 + 3 * 4"
       │
       ▼
┌─────────────┐
│    Lexer    │
└──────┬──────┘
       ▼
┌─────────────┐
│   Parser    │
└──────┬──────┘
       ▼
┌─────────────┐
│ Parse Tree  │
└──────┬──────┘
       ▼
┌─────────────┐
│     AST     │
└──────┬──────┘
       ▼
┌─────────────┐
│  Evaluator  │
└──────┬──────┘
       ▼
      14
```

A precedência matemática é preservada.

Assim:

```text
2 + 3 * 4
```

é interpretado como:

```text
2 + (3 * 4)
```

resultando em:

```text
14
```

Enquanto:

```text
(2 + 3) * 4
```

resulta em:

```text
20
```

---

# 🌳 Abstract Syntax Tree — AST

Após o parsing realizado pelo ANTLR, a expressão é convertida para uma **Abstract Syntax Tree própria do domínio**.

A raiz do modelo é:

```kotlin
sealed interface Expression
```

A AST possui estruturas para representar:

```text
Expression
│
├── NumberLiteral
├── VariableReference
├── BinaryExpression
└── UnaryExpression
```

As operações binárias atualmente representadas são:

```text
ADD
SUBTRACT
MULTIPLY
DIVIDE
POWER
```

Essa separação evita que o restante do Formula Engine fique diretamente dependente da Parse Tree gerada pelo ANTLR.

---

# 🔢 Variáveis

A DSL permite utilizar variáveis dentro das expressões.

Exemplo:

```text
base * altura
```

Durante a avaliação, os valores das variáveis são fornecidos ao motor.

Por exemplo:

```text
base = 10
altura = 5
```

A expressão:

```text
base * altura
```

produz:

```text
50
```

A mesma fórmula pode, portanto, ser executada utilizando diferentes valores.

---

# ▶️ Executando o projeto

O projeto utiliza o **Gradle Wrapper**, portanto não é necessário possuir uma instalação global do Gradle.

Clone o repositório:

```bash
git clone https://github.com/luizleme-tech/dsl-calculos.git
```

Entre no diretório do Formula Engine:

```bash
cd dsl-calculos/formula-engine/formula-engine
```

Em Linux ou macOS, utilize:

```bash
./gradlew
```

No Windows:

```powershell
gradlew.bat
```

---

# 🚀 Executando os exemplos

O projeto possui exemplos executáveis que demonstram o funcionamento da DSL.

Os exemplos podem ser executados diretamente através do **Gradle Wrapper**.

## Linux / macOS

```bash
./gradlew runExamples
```

## Windows

```powershell
gradlew.bat runExamples
```

A execução percorre os exemplos definidos no projeto e demonstra o fluxo completo:

```text
Expressão
    ↓
Parser ANTLR
    ↓
AST
    ↓
Evaluator
    ↓
Resultado
```

Por exemplo, uma expressão como:

```text
2 + 3 * 4
```

deve produzir:

```text
14
```

Enquanto:

```text
(2 + 3) * 4
```

deve produzir:

```text
20
```

---

# 🧪 Executando os testes

Para executar toda a suíte de testes:

### Linux / macOS

```bash
./gradlew test
```

### Windows

```powershell
gradlew.bat test
```

O Gradle irá compilar o projeto e executar os testes automatizados.

---

## Executando um teste específico

Também é possível executar apenas uma classe de testes:

```bash
./gradlew test --tests "NomeDaClasseDeTeste"
```

Ou um teste específico:

```bash
./gradlew test --tests "NomeDaClasseDeTeste.nomeDoTeste"
```

Isso é útil durante o desenvolvimento de novas funcionalidades da linguagem.

---

# ⚠️ Testes de erro

A DSL também possui cenários destinados a verificar expressões inválidas.

Por exemplo:

```text
10 +
```

```text
* 5
```

```text
(10 + 5
```

O objetivo é garantir que uma fórmula inválida não seja silenciosamente interpretada como uma expressão válida.

O motor deve produzir o erro esperado para cada cenário.

Isso permite testar diferentes categorias de problema:

```text
Fórmula
   │
   ├── válida ──────────────► Resultado
   │
   └── inválida
          │
          ▼
       Exceção
```

---

# 🧹 Limpando o projeto

Para remover arquivos gerados pelo build:

### Linux / macOS

```bash
./gradlew clean
```

### Windows

```powershell
gradlew.bat clean
```

---

# 🔨 Build completo

Para limpar, compilar e executar os testes:

### Linux / macOS

```bash
./gradlew clean build
```

### Windows

```powershell
gradlew.bat clean build
```

O fluxo executado pelo Gradle será aproximadamente:

```text
ANTLR
  ↓
geração do Lexer/Parser
  ↓
compilação Kotlin
  ↓
compilação dos testes
  ↓
execução dos testes
  ↓
build
```

---

# 📋 Comandos principais

| Objetivo          | Linux / macOS           | Windows                   |
| ----------------- | ----------------------- | ------------------------- |
| Executar exemplos | `./gradlew runExamples` | `gradlew.bat runExamples` |
| Executar testes   | `./gradlew test`        | `gradlew.bat test`        |
| Build completo    | `./gradlew build`       | `gradlew.bat build`       |
| Limpar projeto    | `./gradlew clean`       | `gradlew.bat clean`       |
| Limpar + build    | `./gradlew clean build` | `gradlew.bat clean build` |

---

# 🗂️ Papel dos exemplos

Os exemplos existentes no projeto têm uma função diferente dos testes automatizados.

### Exemplos

Servem para **visualizar o Formula Engine funcionando**:

```text
./gradlew runExamples
```

Eles permitem acompanhar:

```text
entrada → parsing → AST → avaliação → resultado
```

### Testes

Servem para verificar automaticamente o comportamento esperado:

```text
./gradlew test
```

Eles validam:

* operações matemáticas;
* precedência;
* parênteses;
* operadores;
* variáveis;
* AST;
* avaliação;
* cenários inválidos;
* comportamento esperado em erros.

Dessa forma:

```text
             Formula Engine
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
     Exemplos              Testes
         │                   │
 demonstração           validação
         │                   │
         ▼                   ▼
 ./gradlew              ./gradlew
 runExamples               test
```

---

# 🚧 Próximas evoluções

O Formula Engine está sendo desenvolvido incrementalmente.

Entre as funcionalidades que poderão ser adicionadas futuramente estão:

* funções matemáticas;
* raiz quadrada;
* constantes matemáticas;
* geometria;
* equações;
* função quadrática;
* trigonometria;
* cálculos encadeados;
* fórmulas físicas;
* constantes físicas;
* unidades de medida.

Exemplos futuros poderão incluir:

```text
sqrt(x)
```

```text
PI * raio ^ 2
```

```text
sin(angulo)
```

```text
(-b + sqrt(b^2 - 4*a*c)) / (2*a)
```

Essas funcionalidades somente devem ser consideradas suportadas quando estiverem implementadas e cobertas por testes.

---

# 🎓 Objetivo de estudo

O projeto funciona como laboratório para estudar a construção de uma linguagem do início ao fim:

```text
Gramática
   ↓
ANTLR
   ↓
Lexer
   ↓
Parser
   ↓
Parse Tree
   ↓
AST
   ↓
Evaluator
   ↓
Resultado
```

Entre os conceitos explorados estão:

* Domain-Specific Languages;
* gramáticas formais;
* ANTLR;
* análise léxica;
* análise sintática;
* Parse Tree;
* Abstract Syntax Tree;
* Visitor;
* interpretação;
* precedência;
* associatividade;
* tratamento de erros;
* testes de linguagens.

---

# 🛠️ Tecnologias

* **Kotlin**
* **ANTLR 4**
* **Gradle**
* **JUnit**

---

# 👨‍💻 Autor

**Luiz Leme**

Projeto de estudo e desenvolvimento de uma DSL para cálculos matemáticos.

**Kotlin · ANTLR · DSL · AST · Compiladores · Interpretadores · Matemática**
