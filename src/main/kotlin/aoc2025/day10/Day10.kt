package aoc2025.day10

import catching
import debug
import go
import eventAndDayFromPackage
import helpers.Fraction
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
    val example2 = """
        [###......#] (0,2,4,5,6,7,8,9) (5,7) (0,3,4,7) (4,5) (0,3,4,7,9) (2,5) (4,9) (1,3,5,8) (0,1,3,9) (0,5,6,7,8,9) (0,3) (0,1,2,6,9) (1,3,6,7,8,9) {42,26,20,54,57,38,17,48,19,61}
    """.trimIndent()
    go("part 1 ex", 7) { part1(example) }
    go("part 1") { part1(input) }
    go("part 2 ex 2", 99) { part2(example2) }
    go("part 2 ex", 33) { part2(example) }
    go("part 2", 17133) { part2(input) }
}

typealias Lights = List<Boolean>
typealias Button = List<Boolean>

data class Machine(val lights: Lights, val buttons: List<Button>, val requirements: List<Int>)
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


fun part2(data: String) =
    parse(data).sortedByDescending { it.buttons.sumOf { it.count { it } } }.mapParallel { machine ->

//        val gj = solveGaussJordan(machine.requirements, machine.buttons)
//        val bb = solveBranchAndBound(machine.requirements, machine.buttons)
        val gj = solveGaussJordanAllSolutions(machine.requirements, machine.buttons)
            .minBy { it.sum() }.map { it.toInt() }
        gj.sum()
    }.sum()

private fun solveBranchAndBound(requirements: List<Int>, buttons: List<Button>): List<Int> {
    var best = Int.MAX_VALUE
    val bestSolution = IntArray(buttons.size)
    val currentSolution = IntArray(buttons.size)

    fun branchAndBound(requirements: List<Int>, buttons: List<IndexedValue<Button>>, currentSum: Int) {
        if (currentSum >= best) return
        if (requirements.all { it == 0 }) {
            best = currentSum
            currentSolution.copyInto(bestSolution)
            return
        }
        if (requirements.any { it < 0 }) return
        if (buttons.none()) return

        var nextColumn = 0
        var contributors = Int.MAX_VALUE
        requirements.indices.forEach { column ->
            if (requirements[column] > 0) {
                var i = 0
                var count = 0
                while (i < buttons.size) {
                    if (buttons[i].value[column]) count++
                    if (count >= contributors) return@forEach
                    i++
                }
                if (count == 0) return
                contributors = count
                nextColumn = column
                if (contributors == 1) return@forEach
            }
        }

        if (contributors == 1) {
            val nextBtn = buttons.first { it.value[nextColumn] }
            val times = requirements[nextColumn]
            if (currentSum + times >= best) return
            currentSolution[nextBtn.index] += times
            branchAndBound(
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
                    branchAndBound(
                        requirements.discharge(nextBtn.value, times),
                        buttons.filterNot { it.index == nextBtn.index },
                        currentSum + times
                    )
                    currentSolution[nextBtn.index] -= times
                }
            }
        }

    }

    branchAndBound(requirements, buttons.withIndex().toList(), 0)
    return bestSolution.toList()
}

fun solveGaussJordanAllSolutions(target: List<Int>, buttons: List<Button>): List<List<Long>> {
    val rows = target.size
    val cols = buttons.size

    val matrix = Array(rows) { r ->
        Array(cols + 1) { c ->
            if (c < cols) Fraction.from(buttons[c][r])
            else Fraction.from(target[r])
        }
    }

    val pivotColForRow = IntArray(rows) { -1 }
    val isPivotCol = BooleanArray(cols)
    var pivotRow = 0

    for (c in 0 until cols) {
        if (pivotRow == rows) break

        var maxRow = pivotRow
        while (maxRow < rows && matrix[maxRow][c].num == 0L) maxRow++
        if (maxRow == rows) continue

        val temp = matrix[pivotRow]
        matrix[pivotRow] = matrix[maxRow]
        matrix[maxRow] = temp

        val pivotVal = matrix[pivotRow][c]
        for (j in c..cols) matrix[pivotRow][j] = matrix[pivotRow][j] / pivotVal

        for (r in 0 until rows) {
            if (r != pivotRow) {
                val factor = matrix[r][c]
                if (factor.num != 0L) {
                    for (j in c..cols) matrix[r][j] = matrix[r][j] - (factor * matrix[pivotRow][j])
                }
            }
        }

        pivotColForRow[pivotRow] = c
        isPivotCol[c] = true
        pivotRow++
    }

    for (r in pivotRow until rows) {
        if (matrix[r][cols].num != 0L) return emptyList()
    }

    val freeVarsIndices = (0 until cols).filter { !isPivotCol[it] }
    val allSolutions = mutableListOf<List<Long>>()

    if (freeVarsIndices.isEmpty()) {
        val result = LongArray(cols)
        for (r in 0 until pivotRow) {
            val res = matrix[r][cols]
            if (!res.isInteger() || res.isNegative()) return emptyList()
            result[pivotColForRow[r]] = res.toLong()
        }
        return listOf(result.toList())
    }

    val limit = target.max().toLong()

    fun search(idx: Int, freeVarsValues: LongArray) {
        if (idx == freeVarsIndices.size) {
            val currentSolution = LongArray(cols)
            for (i in freeVarsIndices.indices) {
                currentSolution[freeVarsIndices[i]] = freeVarsValues[i]
            }

            var possible = true
            for (r in 0 until pivotRow) {
                var valPivot = matrix[r][cols] // Stała

                for (i in freeVarsIndices.indices) {
                    val colIdx = freeVarsIndices[i]
                    val coeff = matrix[r][colIdx]
                    if (coeff.num != 0L) {
                        valPivot = valPivot - (coeff * Fraction(freeVarsValues[i]))
                    }
                }

                if (!valPivot.isInteger() || valPivot.isNegative()) {
                    possible = false
                    break
                }
                currentSolution[pivotColForRow[r]] = valPivot.toLong()
            }

            if (possible) {
                allSolutions.add(currentSolution.toList())
            }
            return
        }

        for (v in 0L..limit) {
            freeVarsValues[idx] = v
            search(idx + 1, freeVarsValues)
        }
    }

    search(0, LongArray(freeVarsIndices.size))
    return allSolutions
}
