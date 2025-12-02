package aoc2025.day1

import catching
import go
import eventAndDayFromPackage
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    val ex1 = """
        L68
        L30
        R48
        L5
        R60
        L55
        L1
        L99
        R14
        L82

    """.trimIndent()
    go("part 1", 989) { part1(input) }
    go("part2ex1", 6) { part2(ex1) }
    go("part 2", 5941) { part2(input) }
}

private fun solve(data: String, counterOp: (prev: Int, turns: Int) -> Int): Int =
    data.trim().lines().fold(50 to 0) { (prev, zeros), line ->
        val turns = if (line.first() == 'R') line.drop(1).toInt() else -line.drop(1).toInt()
        val next = (prev + turns).mod(100)
        next to zeros + counterOp(prev, turns)
    }.second

fun part1(data: String) = solve(data) { prev, turns ->
    if ((prev + turns).mod(100) == 0) 1 else 0
}

fun part2(data: String) = solve(data) { prev, turns ->
    when {
        turns > 0 -> (prev + turns) / 100
        else -> ((100 - prev) % 100 - turns) / 100
    }
}
