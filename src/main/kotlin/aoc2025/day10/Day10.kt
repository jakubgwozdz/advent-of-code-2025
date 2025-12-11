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
    go("part 1 ex", 7) { part1(example) }
    go("part 1") { part1(input) }
    go("part 2 ex", 33) { part2(example) }
    go("part 2", 17133) { part2(input) }
}

typealias Lights = List<Boolean>
typealias Button = List<Boolean>

data class Machine(val lights: Lights, val buttons: List<Button>, val requirements: List<Int>)

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


fun part2(data: String) = parse(data).sortedByDescending { it.buttons.sumOf { it.count { it } } }.map { machine ->

    solveExact(machine.requirements, machine.buttons)
    solveBB(machine.requirements, machine.buttons)
}.sum()

private fun solveBB(requirements: List<Int>, buttons: List<Button>): Int {
    var best = Int.MAX_VALUE
    val currentSolution = IntArray(buttons.size)

    fun branchAndBound(requirements: List<Int>, buttons: List<IndexedValue<Button>>, currentSum: Int) {
        if (currentSum >= best) return
        if (requirements.all { it == 0 }) {
            best = currentSum
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
    return best.debug { "partial: $it" }
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


/**
 * Rozwiązuje układ równań Ax = b w liczbach naturalnych (>=0), minimalizując sum(x).
 * Używa eliminacji Gaussa na ułamkach, a następnie przeszukuje zmienne wolne.
 */
fun solveExact(target: List<Int>, buttons: List<Button>): Long {
    val rows = target.size
    val cols = buttons.size

    // 1. Budujemy macierz rozszerzoną [A | b] używając ułamków
    val matrix = Array(rows) { r ->
        Array(cols + 1) { c ->
            if (c < cols) Fraction.from(buttons[c][r])
            else Fraction.from(target[r])
        }
    }

    // 2. Eliminacja Gaussa-Jordana (RREF)
    val pivotColForRow = IntArray(rows) { -1 }
    val isPivotCol = BooleanArray(cols)
    var pivotRow = 0

    for (c in 0 until cols) {
        if (pivotRow == rows) break

        // Wybór pivota (szukamy niezerowego)
        var maxRow = pivotRow
        while (maxRow < rows && matrix[maxRow][c].num == 0L) {
            maxRow++
        }
        if (maxRow == rows) continue // Kolumna zerowa

        // Zamiana wierszy
        val temp = matrix[pivotRow]
        matrix[pivotRow] = matrix[maxRow]
        matrix[maxRow] = temp

        // Normalizacja wiersza (dzielimy przez pivot)
        val pivotVal = matrix[pivotRow][c]
        for (j in c..cols) matrix[pivotRow][j] = matrix[pivotRow][j] / pivotVal

        // Zerowanie kolumny w innych wierszach
        for (r in 0 until rows) {
            if (r != pivotRow) {
                val factor = matrix[r][c]
                if (factor.num != 0L) {
                    for (j in c..cols) {
                        matrix[r][j] = matrix[r][j] - (factor * matrix[pivotRow][j])
                    }
                }
            }
        }

        pivotColForRow[pivotRow] = c
        isPivotCol[c] = true
        pivotRow++
    }

    // Sprawdzenie sprzeczności: wiersz [0 0 ... 0 | !=0]
    for (r in pivotRow until rows) {
        if (matrix[r][cols].num != 0L) error("Inconsistent system")
    }

    // 3. Identyfikacja zmiennych wolnych
    val freeVarsIndices = (0 until cols).filter { !isPivotCol[it] }

    // Jeśli brak zmiennych wolnych (układ oznaczony), sprawdzamy to jedno rozwiązanie
    if (freeVarsIndices.isEmpty()) {
        var totalMoves = 0L
        for (r in 0 until pivotRow) {
            val res = matrix[r][cols]
            if (!res.isInteger() || res.isNegative()) error("No solution")
            totalMoves += res.toLong()
        }
        return totalMoves
    }

    // 4. Przeszukiwanie przestrzeni zmiennych wolnych
    var minTotalMoves = Long.MAX_VALUE
    var solutionFound = false

    // Upper bound dla zmiennych wolnych.
    // Każdy przycisk (wektor) ma wagi nieujemne (0 lub 1).
    // Zatem zmienna x_i nie może być większa niż maksymalna wartość w wektorze docelowym.
    // (Bardziej rygorystyczne: min(target[k] / button[k]) dla button[k] > 0)
    val limit = target.max().toLong()

    fun search(idx: Int, freeVarsValues: LongArray) {
        // Pruning: Jeśli suma samych zmiennych wolnych przekracza minimum, to nie ma sensu (bo zależne też są >= 0)
        // Uwaga: To działa tylko, jeśli rozwiązanie pivotów też dodaje do sumy (a dodaje, bo >= 0).
        if (freeVarsValues.sum() >= minTotalMoves) return

        if (idx == freeVarsIndices.size) {
            // Wszystkie wolne ustalone -> wyliczamy zależne
            var currentTotal = 0L
            // Dodajemy koszt zmiennych wolnych
            for (v in freeVarsValues) currentTotal += v
            if (currentTotal >= minTotalMoves) return

            var possible = true
            for (r in 0 until pivotRow) {
                // Równanie: x_pivot + sum(coeff * x_free) = constant
                // x_pivot = constant - sum(coeff * x_free)
                var valPivot = matrix[r][cols] // Stała

                for (i in freeVarsIndices.indices) {
                    val colIdx = freeVarsIndices[i]
                    val coeff = matrix[r][colIdx]
                    if (coeff.num != 0L) {
                        val contribution = coeff * Fraction(freeVarsValues[i])
                        valPivot = valPivot - contribution
                    }
                }

                if (!valPivot.isInteger() || valPivot.isNegative()) {
                    possible = false
                    break
                }
                currentTotal += valPivot.toLong()
            }

            if (possible) {
                if (currentTotal < minTotalMoves) {
                    minTotalMoves = currentTotal
                    solutionFound = true
                }
            }
            return
        }

        // Iteracja dla kolejnej zmiennej wolnej
        // Zmieniamy kolejność pętli 0..limit vs limit downTo 0 w zależności czy chcemy szybko znaleźć *jakieś* rozwiązanie,
        // czy szybko znaleźć *małe*. Dla minimalizacji lepiej od 0 w górę.
        for (v in 0L..limit) {
            freeVarsValues[idx] = v
            search(idx + 1, freeVarsValues)
            // Prosty heuristic pruning w pętli:
            // Jeśli już po ustawieniu jednej zmiennej przekraczamy limit (bo funkcja jest rosnąca), przerywamy pętlę
            if (freeVarsValues.take(idx + 1).sum() >= minTotalMoves) break
        }
    }

    search(0, LongArray(freeVarsIndices.size))
    require(solutionFound) { "No solution found" }
    return minTotalMoves
}
