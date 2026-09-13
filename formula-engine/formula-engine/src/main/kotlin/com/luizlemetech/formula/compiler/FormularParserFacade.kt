package com.luizlemetech.formula.compiler

import com.luizlemetech.formula.antlr.FormulaLexer
import com.luizlemetech.formula.antlr.FormulaParser
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream

class FormularParserFacade {

    fun parse(source: String): FormulaParser.AssignmentContext {
        val input = CharStreams.fromString(source)
        val lexer = FormulaLexer(input)
        val tokens = CommonTokenStream(lexer)
        val parser = FormulaParser(tokens)

        return parser.assignment()
    }
}