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
        appendLine("remaining sum: $remainingSum at step $steps")
        appendLine(toGo.joinToString(" ") { it.toString().padStart(2) })
        buttons.forEach { appendLine(it.buttonsToString()) }
    }.trim()

    fun isValid() = toGo.filterIndexed { index, v -> v < 0 || v > 0 && buttons.none { it[index] } }.isEmpty()

    fun normalize() = toGo.indices.filter { index -> toGo[index] == 0 && buttons.none { it[index] } }
        .takeIf { it.isNotEmpty() }?.let {
            copy(
                toGo = toGo.filterIndexed { index, _ -> index !in it },
                buttons = buttons.map { button -> button.filterIndexed { index, _ -> index !in it } }
            )
        } ?: this

}

private fun Buttons.filtered(nextToGo: List<Int>): List<Button> = filter { b ->
    b.zip(nextToGo).all { (a, v) -> !a || v > 0 }
}

private fun Button.buttonsToString(): String = joinToString(" ") { if (it) " #" else " ." }

fun part2(data: String) = parse(data).sumOf { machine ->

    return@sumOf solveIntegerSystem(machine.buttons, machine.requirements).debug()!!.sum().toInt()

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
        (/*buttons.filtered(toGo).takeIf { it != buttons }
            ?.let {
                State(toGo, it, steps).debug { "reduced buttons: $it" }
                listOf(State(toGo, it, steps) to 0)
            }
            ?: */buttons.possibleSingle()?.let { (i, b) ->
//                State(toGo, buttons, steps).debug { "found single on $i: $it" }
            val times = toGo[i]
            val nextToGo = toGo.discharge(b, times)
            val nextButtons = buttons.filtered(nextToGo)
            listOf(State(nextToGo, nextButtons, steps + times).normalize() to times)
        }
//            ?: buttons.possibleCouple()?.let { (i, b1, b2) ->
////                State(toGo, buttons, steps).debug { "found couples on $i: $it" }
//                val times = toGo[i]
//                (0..times).map { times1 ->
//                    val times2 = times - times1
//                    val nextToGo = toGo.discharge(b1, times1).discharge(b2, times2)
//                    val nextButtons = buttons.filtered(nextToGo)
//                    State(nextToGo, nextButtons, steps + times) to times
//                }
//            }
            ?: toGo.withIndex().minBy { buttons.count { b -> b[it.index] }.let { if (it == 0) 100 else it } }
                .let { (i, times) ->
                    State(toGo, buttons, steps).debug { "will try split on $i: $it" }
                    val btns = buttons.filter { it[i] }
//                    .debug { "buttons to split: ${it.map { it.buttonsToString() }}" }
                    generateDistributions(times, btns.size)
                        .map { v ->
                            val nextToGo = v.zip(btns).fold(toGo) { acc, (v, b) -> acc.discharge(b, v) }
                            val nextButtons = (buttons - btns).filtered(nextToGo)
                            State(nextToGo, nextButtons, steps + times).normalize() to times
                        }
//                        .filter { (state) -> state.isValid() }
                        .toList()
                        .debug { "generated states: ${it.count()}" }
                }
//            ?: buttons.map { button ->
//                val nextToGo = toGo.discharge(button)
//                val nextButtons = buttons.filtered(nextToGo)
//                State(nextToGo, nextButtons, steps + 1) to 1
//            }
                )
            .filter { (state) -> state.isValid() }
//            .filterNot { (state) -> state.toGo in visited }
//            .onEach { (state) -> visited.add(state.toGo) }
    }

    result
        .debug { "result: $it" }
}

fun generateDistributions(total: Int, n: Int): Sequence<List<Int>> = when {
    n == 1 -> sequenceOf(listOf(total))
    n <= 0 -> emptySequence()
    else -> sequence {
        for (i in 0..total) {
            val remainingSum = total - i
            val subDistributions = generateDistributions(remainingSum, n - 1)
            for (sub in subDistributions) {
                yield((listOf(i) + sub))
            }
        }
    }
}

private fun parse(data: String): List<Machine> = data.reader().readLines().map { line ->
    val splits = line.split(" ").filter { it.isNotBlank() }
    val lights = splits.first().drop(1).dropLast(1).map { it == '#' }
    val buttons = splits.drop(1).dropLast(1).map { w ->
        val indices = w.drop(1).dropLast(1).split(",").map { it.toInt() }
        List(lights.size) { it in indices }
    }.sortedBy { it.buttonsToString() }
    val requirements = splits.last().drop(1).dropLast(1).split(",").map { it.toInt() }
    Machine(lights, buttons, requirements)
}

fun solveIntegerSystem(buttons: Buttons, requirements: List<Int>): List<Int> {
    val indexedButtons = buttons.mapIndexed { index, vals -> index to vals }
    val currentSolution = IntArray(buttons.size)

    val context = SearchContext(
        minTotalMoves = Int.MAX_VALUE,
        bestSolution = null
    )

    solveBranchAndBound(indexedButtons, requirements, currentSolution, 0, context)

    return context.bestSolution ?: error("no solution found")
}

private class SearchContext(
    var minTotalMoves: Int,
    var bestSolution: List<Int>?
)

private fun solveBranchAndBound(
    availableButtons: List<Pair<Int, Button>>,
    currentReqs: List<Int>,
    solutionAcc: IntArray,
    currentSum: Int,
    ctx: SearchContext
) {
    if (currentSum >= ctx.minTotalMoves) return

    if (currentReqs.all { it == 0 }) {
        ctx.minTotalMoves = currentSum
        ctx.bestSolution = solutionAcc.toList() // Kopia tablicy
        return
    }

    if (currentReqs.any { it < 0 }) return
    if (availableButtons.isEmpty()) return

    var bestRow = -1
    var minContributors = Int.MAX_VALUE

    for (row in currentReqs.indices) {
        if (currentReqs[row] > 0) {
            val count = availableButtons.count { it.second[row] }
            if (count == 0) return
            if (count < minContributors) {
                minContributors = count
                bestRow = row
            }
            if (minContributors == 1) break
        }
    }

    if (minContributors == 1) {
        val (btnIdx, btnMask) = availableButtons.first { it.second[bestRow] }
        val needed = currentReqs[bestRow]

        if (currentSum + needed >= ctx.minTotalMoves) return

        solutionAcc[btnIdx] += needed
        val nextReqs = currentReqs.mapIndexed { i, v -> if (btnMask[i]) v - needed else v }

        solveBranchAndBound(
            availableButtons.filter { it.first != btnIdx },
            nextReqs,
            solutionAcc,
            currentSum + needed,
            ctx
        )

        solutionAcc[btnIdx] -= needed

    } else {
        val (btnIdx, btnMask) = availableButtons.first { it.second[bestRow] }
        val maxUsage = currentReqs[bestRow]

        for (usage in maxUsage downTo 0) {
            if (currentSum + usage >= ctx.minTotalMoves) continue

            solutionAcc[btnIdx] += usage
            val nextReqs = currentReqs.mapIndexed { i, v -> if (btnMask[i]) v - usage else v }

            solveBranchAndBound(
                availableButtons.filter { it.first != btnIdx },
                nextReqs,
                solutionAcc,
                currentSum + usage,
                ctx
            )

            solutionAcc[btnIdx] -= usage
        }
    }
}
