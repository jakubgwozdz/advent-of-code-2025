package aoc2025.day12

import catching
import debug
import go
import eventAndDayFromPackage
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    go("part 1") { part1(input) }
}

data class Region(val w: Int, val h: Int, val counts: List<Int>) {
    fun pack(): Boolean {
        if (counts.sum() <= (w / 3) * (h / 3)) return true
            .debug { "Trivially fits by count" }
        if (counts.sumOf { n -> n * 7 } > h * w) return false
            .debug { "Too much area needed" }
        TODO("Implement packing algorithm")
    }
}

fun part1(data: String) = parse(data).count { region -> region.pack() }

val regionRegex = """(\d+)x(\d+):(( \d+){6})""".toRegex()
private fun parse(data: String): List<Region> = data.split("\n\n").last().reader().readLines().map { line ->
    val match = regionRegex.matchEntire(line) ?: error("Invalid region line: $line")
    val (w, h, countsStr) = match.destructured
    val counts = countsStr.trim().split(" ").map { it.toInt() }
    Region(w.toInt(), h.toInt(), counts)
}

fun part2(data: String): Any {
    return data.length to data.lines().size
}
