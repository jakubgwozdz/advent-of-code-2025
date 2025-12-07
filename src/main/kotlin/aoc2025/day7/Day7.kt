package aoc2025.day7

import catching
import go
import eventAndDayFromPackage
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }

    val ex1 = """
        .......S.......
        ...............
        .......^.......
        ...............
        ......^.^......
        ...............
        .....^.^.^.....
        ...............
        ....^.^...^....
        ...............
        ...^.^...^.^...
        ...............
        ..^...^.....^..
        ...............
        .^.^.^.^.^...^.
        ...............
    """.trimIndent()
    val input = provideInput(event, day)
    go("part1ex1", 21) { part1(ex1) }
    go("part 1") { part1(input) }
    go("part2ex1", 40) { part2(ex1) }
    go("part 2", 171692855075500) { part2(input) }
}

fun part1(data: String): Any {
    val lines = data.reader().readLines()
    var splits = 0
    var beams = setOf<Int>()
    lines.forEach { line ->
        beams = buildSet {
            if (beams.isEmpty()) add(line.indexOf('S'))
            else beams.forEach { i ->
                if (line[i] == '^') {
                    add(i - 1)
                    add(i + 1)
                    splits++
                } else {
                    add(i)
                }
            }
        }
    }
    return splits
}

fun part2(data: String): Any {
    val lines = data.reader().readLines()
    val size = lines.last().length
    var timelines = List(size) { 1L }

    lines.reversed().forEach { line ->
        timelines = List(size) { i ->
            if (line[i] == '^') timelines.getOrElse(i - 1) { 0 } + timelines.getOrElse(i + 1) { 0 }
            else timelines[i]
        }
    }

    return timelines[lines.first().indexOf('S')]
}
