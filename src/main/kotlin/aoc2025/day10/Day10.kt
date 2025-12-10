package aoc2025.day10

import catching
import debug
import go
import eventAndDayFromPackage
import helpers.search.dijkstra
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    val example = """
        [.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
        [...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
        [.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}
    """.trimIndent()
    go("part 1 ex", 7) { part1(example) }
    go("part 1") { part1(input) }
    go("part 2 ex", 33) { part2(example) }
    go("part 2") { part2(input) }
}

typealias Lights = Set<Int>

data class Machine(val lights: Lights, val buttons: List<Set<Int>>, val requirements: List<Int>)

fun part1(data: String): Any = parse(data).sumOf { machine ->
    dijkstra(
        start = emptySet(),
        endPredicate = { it == machine.lights },
        priority = compareBy<Pair<Lights, Int>> { it.second }.thenComparingInt { (it.first - machine.lights).size + (machine.lights - it.first).size },
    ) { lights ->
        machine.buttons.map { l ->
            buildSet { addAll(lights); l.forEach { if (it in this) remove(it) else add(it) } } to 1
        }
    }
}

fun part2(data: String) = parse(data).sumOf { machine ->
    val t = machine.requirements//.mapIndexed { index, i -> if (index in machine.lights) i else 0 }
    dijkstra(
        start = t,
        endPredicate = { it.all { it == 0 } },
        priority = compareBy<Pair<List<Int>, Int>> { it.second }.thenComparingInt { it.first.sum() },
    ) { lights ->
        machine.buttons.map { l ->
            lights.mapIndexed { index, i -> if (index in l) i - 1 else i } to 1
        }.filter { (l, _) -> l.all { it >= 0 } }
    }.debug { "$it for $machine" }
}


private fun parse(data: String): List<Machine> = data.reader().readLines().map { line ->
    val splits = line.split(" ").filter { it.isNotBlank() }
    val lights = splits.first().drop(1).dropLast(1).withIndex().filter { it.value == '#' }.map { it.index }.toSet()
    val buttons = splits.drop(1).dropLast(1).map { w ->
        w.drop(1).dropLast(1).split(",").map { it.toInt() }.toSet()
    }
    val requirements = splits.last().drop(1).dropLast(1).split(",").map { it.toInt() }
    Machine(lights, buttons, requirements)
}

