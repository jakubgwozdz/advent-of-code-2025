package aoc2025.day12

import catching
import eventAndDayFromPackage
import go
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    go("part 1", 440) { part1(input) }
}

fun part1(data: String) = data.split("\n\n").last().reader().readLines().count { line ->
    val ints = line.split("x", ": ", " ").map(String::toInt)
    ints.subList(2, ints.size).sum() <= (ints[0] / 3) * (ints[1] / 3)
}
