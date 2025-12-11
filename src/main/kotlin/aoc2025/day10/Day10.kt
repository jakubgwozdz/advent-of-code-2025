package aoc2025.day10

import catching
import debug
import go
import eventAndDayFromPackage
import helpers.search.dijkstra
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import provideInput
import kotlin.collections.indices

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
    go("part 2", 17133) { part2(input) }
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

private fun Button.buttonsToString(): String = joinToString(" ") { if (it) " #" else " ." }


fun <T, R> List<T>.mapParallel(op: (T) -> R) = runBlocking { map { async(Dispatchers.Default) { op(it) } }.awaitAll() }


fun part2(data: String) = parse(data).sortedByDescending { it.requirements.sum() }.mapParallel { machine ->

    var best = Int.MAX_VALUE
    val currentSolution = IntArray(machine.buttons.size)

    fun solveDSF(requirements: List<Int>, buttons: Iterable<IndexedValue<Button>>, currentSum: Int) {
        if (currentSum >= best) return
        if (requirements.all { it == 0 }) {
            best = currentSum
            return
        }
        if (requirements.any { it < 0 }) return
        if (buttons.none()) return

        var nextColumn = 0
        var contributors = Int.MAX_VALUE
        requirements.indices.forEach {column ->
           if (requirements[column] > 0) {
               val count = buttons.count { it.value[column] }
               if (count == 0) return
               if (count < contributors) {
                   contributors = count
                   nextColumn = column
               }
               if (contributors == 1) return@forEach
           }
        }

        if (contributors == 1) {
            val nextBtn = buttons.first { it.value[nextColumn] }
            val times = requirements[nextColumn]
            if (currentSum + times >= best) return
            currentSolution[nextBtn.index] += times
            solveDSF(
                requirements.discharge(nextBtn.value, times),
                buttons.filterNot { it.index == nextBtn.index },
                currentSum + times
            )
            currentSolution[nextBtn.index] -= times
        } else {
            val nextBtn = buttons.first { it.value[nextColumn] }
            (requirements[nextColumn] downTo 0).forEach { times ->
                if (currentSum + times < best) {
                    currentSolution[nextBtn.index] += times
                    solveDSF(
                        requirements.discharge(nextBtn.value, times),
                        buttons.filterNot { it.index == nextBtn.index },
                        currentSum + times
                    )
                    currentSolution[nextBtn.index] -= times
                }
            }
        }

    }

    solveDSF(machine.requirements, machine.buttons.withIndex(), 0)
    return@mapParallel best
}.sum()

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

