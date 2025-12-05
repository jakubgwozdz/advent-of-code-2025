package aoc2025.day5

import catching
import go
import eventAndDayFromPackage
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    go("part 1", 811) { part1(input) }
    go("part 2", 338189277144473) { part2(input) }
}

fun part1(data: String): Any {
    val lines = data.reader().readLines()
    val ranges = lines.takeWhile { it.isNotBlank() }.map { it.split("-").let { (a, b) -> a.toLong()..b.toLong() } }
    val ids = lines.dropWhile { it.isNotBlank() }.drop(1).map { it.toLong() }
    return ids.count { id -> ranges.any { it.contains(id) } }
}

fun part2(data: String): Long {
    val ranges = data.reader().readLines().takeWhile { it.isNotBlank() }
        .map { it.split("-").let { (a, b) -> a.toLong()..b.toLong() } }

    var result = 0L
    var current = -1L
    ranges.sortedBy(LongRange::first).forEach { next ->
        when  {
            next.first > current -> { // new range
                result += (next.last - next.first + 1)
                current = next.last
            }
            next.first <= current && next.last > current -> { // extend
                result += (next.last - current)
                current = next.last
            }
            else -> {} // skip, overlapping
        }
    }
    return result
}
