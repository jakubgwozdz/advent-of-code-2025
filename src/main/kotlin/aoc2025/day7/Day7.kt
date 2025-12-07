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
    val strings = data.reader().readLines().filterIndexed { i, s -> i % 2 == 0 }
    val start = strings.first().indexOf('S')
    return strings.drop(1).fold(setOf(start) to 0) { (acc, count), s ->
        val newAcc = mutableSetOf<Int>()
        var newCount = count
        acc.forEach { i ->
            if (s[i] == '^') {
                newAcc += i - 1
                newAcc += i + 1
                newCount++
            } else newAcc += i
        }
        newAcc to newCount
    }.second
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
