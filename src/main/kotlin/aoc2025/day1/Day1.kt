package aoc2025.day1

import debug
import go
import eventAndDayFromPackage
import provideInput

fun main() {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    go("part 1") { part1(input) }
    go("part 2") { part2(input) }
}

fun part1(data: String): Any {
    return data
}

fun part2(data: String): Any {
    return data.length to data.lines().size
}
