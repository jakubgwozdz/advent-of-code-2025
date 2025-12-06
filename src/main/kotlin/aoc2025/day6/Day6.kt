package aoc2025.day6

import catching
import debug
import go
import eventAndDayFromPackage
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    go("part 1", 5782351442566) { part1(input) }
    val p2ex = """
        123 328  51 64 
         45 64  387 23 
          6 98  215 314
        *   +   *   +  
    """.trimIndent()
    go("part2ex1", 3263827) { part2(p2ex) }
    go("part 2", 10194584711842) { part2(input) }
}

fun part1(data: String): Any {
    val pieces = data.reader().readLines().map { it.split(Regex("\\s+")).filter(String::isNotBlank) }
    val arguments = pieces.dropLast(1).map { it.map(String::toLong) }
    return pieces.last().mapIndexed { index, string ->
        val op = string[0]
        val args = arguments.map { it[index] }
        calc(op, args)
    }.sum()
}

private fun calc(op: Char, args: List<Long>): Long = when (op) {
    '+' -> args.sum()
    '*' -> args.fold(1L) { acc, list -> acc * list }
    else -> error("Unknown op $op")
}

fun part2(data: String): Any {
    val lines = data.reader().readLines()
    val parts = lines.last().withIndex().filter { it.value != ' ' }.map { it.index }.let {
        it.windowed(2).map { (a, b) -> a..<b-1 } + listOf(it.last()..<lines.last().length) }

    return parts.sumOf { range ->
        val args = range.map { c->(0..<lines.lastIndex).map { r->lines[r][c] }.joinToString("") }
            .filter { it.isNotBlank() }.map { it.trim().toLong() }
        val op = lines.last()[range.first]
        calc(op, args)
    }
}
