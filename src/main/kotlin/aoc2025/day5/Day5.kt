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
    var currentEnd = -1L
    ranges.sortedBy(LongRange::first).forEach { range ->
        when  {
            range.first > currentEnd -> { // new range
                result += (range.last - range.first + 1)
                currentEnd = range.last
            }
            range.last > currentEnd -> { // extend
                result += (range.last - currentEnd)
                currentEnd = range.last
            }
            else -> {} // skip, overlapping
        }
    }
    return result
}
