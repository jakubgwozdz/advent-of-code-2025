package aoc2025.day10

import catching
import debug
import go
import eventAndDayFromPackage
import helpers.search.astar
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
typealias Button = Set<Int>
typealias Buttons = List<Button>

data class Machine(val lights: Lights, val buttons: Buttons, val requirements: List<Int>)

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
    machine.debug()
    data class State(val toGo: List<Int>, val buttons: Buttons, val remainingSum: Int = toGo.sum())

    val visited = mutableSetOf<List<Int>>()
    dijkstra(
        start = State(machine.requirements, machine.buttons),
        endPredicate = { state -> state.toGo.all { it == 0 } },
//        heuristics =  { state -> state.remainingSum },
        priority = compareBy(Pair<State, Int>::second).thenComparingInt { it.first.remainingSum },
    ) { (toGo, buttons) ->
        buttons.map { button ->
            val nextToGo = toGo.mapIndexed { index, i -> if (index in button) i - 1 else i }
            val nextButtons = buttons.filter { l -> l.all { nextToGo[it] > 0 } }
            State(nextToGo, nextButtons) to 1
        }
            .filterNot { (state) -> state.toGo in visited }
            .onEach { (state) -> visited.add(state.toGo) }
//            .onEach { (l, _) -> check(l.first.all { it >= 0 }) }
//            .filter { (l, _) -> l.first.all { it >= 0 } }
    }
        .debug { "result: $it" }
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

