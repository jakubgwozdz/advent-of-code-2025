package aoc2025.day6

import catching
import go
import eventAndDayFromPackage
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    go("part 1", 5782351442566) { part1(input) }
    go("part 2", 10194584711842) { part2(input) }
}

fun part1(data: String) = solve(data) { strings ->
    strings.map { it.trim().toLong() }
}

fun part2(data: String) = solve(data) { strings ->
    strings.transposed().map { it.trim().toLong() }
}

private fun List<String>.transposed(): List<String> =
    first().indices.map { c -> indices.map { r -> this[r][c] }.joinToString("") }

private fun solve(data: String, numbersOp: (List<String>) -> List<Long>): Long {
    val lines = data.reader().readLines()
    val ranges = lines.last().withIndex().filter { it.value != ' ' }.map { it.index }
        .let { it.windowed(2).map { (a, b) -> a..<b - 1 } + listOf(it.last()..<lines.last().length) }
    val parts = ranges.map { range -> lines.map { line -> line.substring(range) } }

    return parts.sumOf { part ->
        val args = numbersOp(part.dropLast(1))
        when (part.last().first()) {
            '+' -> args.reduce(Long::plus)
            '*' -> args.reduce(Long::times)
            else -> error("Unknown op")
        }
    }
}

