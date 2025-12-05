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

fun part2(data: String) = data.reader().readLines().takeWhile { it.isNotBlank() }
    .map { it.split("-").let { (a, b) -> a.toLong()..b.toLong() } }
    .sortedBy(LongRange::first)
    .fold(0L to LongRange.EMPTY) { (count, range), next ->
        when (next.first) {
            in range if next.last in range -> count to range
            in range -> count + (next.last - range.last) to range.first..next.last
            else -> count + (next.last + 1 - next.first) to next
        }
    }.first
