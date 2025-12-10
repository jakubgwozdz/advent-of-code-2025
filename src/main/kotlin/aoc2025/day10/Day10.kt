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

typealias Lights = List<Boolean>
typealias Button = List<Boolean>
typealias Buttons = List<Button>

data class Machine(val lights: Lights, val buttons: Buttons, val requirements: List<Int>)

fun part1(data: String): Any = parse(data).sumOf { machine ->
    dijkstra(
        start = List(machine.lights.size) { false },
        endPredicate = { it == machine.lights },
        priority = compareBy<Pair<Lights, Int>> { it.second }.thenComparingInt { (lights) ->
            lights.zip(machine.lights).count { (a, b) -> a != b }
        },
    ) { lights ->
        machine.buttons.map { button ->
            lights.mapIndexed { i, b -> if (button[i]) !b else b } to 1
        }
    }
}

fun List<Int>.discharge(b: Button, times: Int = 1) = mapIndexed { index, v -> if (b[index]) v - times else v }

fun Buttons.possibleSingle() = if (isEmpty()) null
else first().indices.firstNotNullOfOrNull { i -> singleOrNull { it[i] }?.let { i to it } }

fun Buttons.possibleCouple() = if (size < 2) null
else first().indices.firstNotNullOfOrNull { i ->
    val bs = filter { it[i] }
    if (bs.size != 2) null else {
        val (b1, b2) = bs
        Triple(i, b1, b2)
    }
}

data class State(val toGo: List<Int>, val buttons: Buttons, val steps: Int) {
    val remainingSum: Int = toGo.sum()
    override fun toString(): String = buildString {
        appendLine("remaining sum: $remainingSum")
        appendLine(toGo.joinToString(" ") { it.toString().padStart(2) })
        buttons.forEach { appendLine(it.buttonsToString()) }
    }.trim()

    fun reduceSingles(): State {
        var s1 = this
        while (true) {
            val single = s1.buttons.possibleSingle()
//                .debug { "single switch button: $it" }
            if (single == null) return s1
            val (i, button) = single
            val times = s1.toGo[i]
            val nextToGo = s1.toGo.discharge(button, times)
            s1 = State(
                nextToGo,
                s1.buttons.filtered(nextToGo),
                s1.steps + times
            )
//                .debug { "after pressing [${button.buttonsToString()}] $i times: $it" }
        }
    }

    //    fun reduceImpossible(): State {
//        if (toGo.none { it == 0 })
//    }

//    fun reduceDoubles
}

private fun Buttons.filtered(nextToGo: List<Int>): List<Button> = filter { b ->
    b.zip(nextToGo).all { (a, v) -> !a || v > 0 }
}

private fun Button.buttonsToString(): String = joinToString(" ") { if (it) " #" else " ." }

fun part2(data: String) = parse(data).sumOf { machine ->
    val state = State(machine.requirements, machine.buttons, 0)
        .debug { "initial state: $it" }

//    while (true) {
//        val prevSum = state.remainingSum
//        val prevState = state
//        state = state.reduceSingles()
//        if (prevState == state) break
//    }
//
    var result = state.steps
    val visited = mutableSetOf<List<Int>>()
    result += dijkstra(
        start = state,
        endPredicate = { state -> state.toGo.all { it == 0 } },
//        heuristics =  { state -> state.remainingSum },
        priority = compareBy(Pair<State, Int>::second).thenComparingInt { it.first.remainingSum },
    ) { (toGo, buttons, steps) ->
        (buttons.filtered(toGo).takeIf { it != buttons }?.let { listOf(State(toGo, it, steps) to 0) }
            ?: buttons.possibleSingle()?.let { (i, b) ->
                val times = toGo[i]
                val nextToGo = toGo.discharge(b, times)
                val nextButtons = buttons.filtered(nextToGo)
                listOf(State(nextToGo, nextButtons, steps + times) to times)
            }
            ?: buttons.possibleCouple()?.let { (i, b1, b2) ->
                val times = toGo[i]
                (0..times).map { times1 ->
                    val times2 = times - times1
                    val nextToGo = toGo.discharge(b1, times1).discharge(b2, times2)
                    val nextButtons = buttons.filtered(nextToGo)
                    State(nextToGo, nextButtons, steps + times) to times
                }
                    .filter { (state) -> state.toGo.all { it >= 0 } }

            }
//            ?:emptyList()
//            ?:error("no possible couple")
            ?: buttons.map { button ->
                val nextToGo = toGo.discharge(button)
                val nextButtons = buttons.filtered(nextToGo)
                State(nextToGo, nextButtons, steps + 1) to 1
            })
            .filterNot { (state) -> state.toGo in visited }
            .onEach { (state) -> visited.add(state.toGo) }
    }

    result
        .debug { "result: $it" }
}


private fun parse(data: String): List<Machine> = data.reader().readLines().map { line ->
    val splits = line.split(" ").filter { it.isNotBlank() }
    val lights = splits.first().drop(1).dropLast(1).map { it == '#' }
    val buttons = splits.drop(1).dropLast(1).map { w ->
        val indices = w.drop(1).dropLast(1).split(",").map { it.toInt() }
        List(lights.size) { it in indices }
    }
    val requirements = splits.last().drop(1).dropLast(1).split(",").map { it.toInt() }
    Machine(lights, buttons, requirements)
//        .debug { "$data parsed to $it" }
}

