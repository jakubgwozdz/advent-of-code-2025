package aoc2025.day2

import go
import eventAndDayFromPackage
import provideInput

fun main() {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    go("part 1", 28844599675) { part1(input) }
    go("part 2", 48778605167) { part2(input) }
}

private fun solve(data: String, filterOp: (String) -> Boolean): Long =
    data.trim().split(",").map { it.split("-").let { (a, b) -> a.toLong()..b.toLong() } }
        .flatMap { it.asSequence() }
        .map { it.toString() }
        .filter(filterOp)
        .sumOf { it.toLong() }

fun part1(data: String): Any = solve(data) { s ->
    s.length % 2 == 0 && s.take(s.length / 2) == s.takeLast(s.length / 2)
}

fun part2(data: String): Any = solve(data) { s ->
    (1..s.length / 2).any { c ->
        s.chunked(c).let { l -> l.all { p -> p == l.first() } }
    }
}
