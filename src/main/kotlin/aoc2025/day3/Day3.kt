package aoc2025.day3

import catching
import debug
import go
import eventAndDayFromPackage
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    go("part 1", 16973) { part1(input) }
    go("part 2", 168027167146027) { part2(input) }
}

fun part1(data: String) = solve(data, 2)
fun part2(data: String) = solve(data, 12)

private fun solve(data: String, count: Int) = data.lineSequence().filterNot { it.isBlank() }
    .sumOf { line ->
        var result = 0L
        var rest = line
        repeat(count) { i ->
            val n = rest.dropLast(count - i - 1).withIndex().groupBy { it.value }
                .entries.maxBy { it.key }
                .value.minBy { it.index }
            rest = rest.drop(n.index + 1)
            result = result * 10 + n.value.digitToInt()
        }
        result
    }
