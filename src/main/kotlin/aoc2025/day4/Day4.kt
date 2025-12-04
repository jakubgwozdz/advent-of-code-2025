package aoc2025.day4

import catching
import eventAndDayFromPackage
import go
import helpers.*
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    go("part 1", 1502) { part1(input) }
    go("part 2", 9083) { part2(input) }
}

fun part1(data: String): Any {
    val rolls = data.lines().findAll('@')
    return rolls.count(rolls::accessible)
}

fun part2(data: String): Any {
    val rolls = data.lines().findAll('@').toMutableSet()
    var result = 0
    while (true) {
        val removed = rolls.filter(rolls::accessible).toSet()
        result += removed.size
        rolls -= removed
        if (removed.isEmpty()) break
    }
    return result
}

private fun Set<Pos>.accessible(pos: Pos): Boolean =
    listOf(pos.n(), pos.e(), pos.s(), pos.w(), pos.ne(), pos.se(), pos.sw(), pos.nw()).count(this::contains) < 4

