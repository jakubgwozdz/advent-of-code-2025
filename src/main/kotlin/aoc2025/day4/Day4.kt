package aoc2025.day4

import catching
import debug
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

fun part1(data: String): Any = parse(data).sumOf { it.count { it in 0..<4 } }

fun part2(data: String): Any {
    val arrays = parse(data)
    var toRemove = buildList {
        arrays.forEachIndexed { row, line ->
            line.forEachIndexed { col, v ->
                if (v in 0..<4) add(longPos(row, col)).also { arrays[row][col] = -1 }
            }
        }
    }
    var result = toRemove.size
    while (toRemove.isNotEmpty()) {
        toRemove = buildList {
            toRemove.forEach { pos ->
                pos.neighbours8().forEach { p1 ->
                    val (r1, c1) = p1
                    if (r1 in arrays.indices && c1 in arrays[r1].indices && arrays[r1][c1] > 3) {
                        arrays[r1][c1]--
                        if (arrays[r1][c1] == 3) add(p1).also { arrays[r1][c1] = -1 }
                    }
                }
            }
        }
        result += toRemove.size
    }
    return result
}

private fun parse(data: String): List<IntArray> {
    // parse, O(n)
    val lines = data.reader().readLines()
    val array = lines.map { IntArray(it.length) }
    lines.forEachIndexed { row, line ->
        line.forEachIndexed { col, c ->
            if (c == '@') {
                if (lines.getOrNull(row)?.getOrNull(col + 1) == '@') {
                    array[row][col]++
                    array[row][col + 1]++
                }
                if (lines.getOrNull(row + 1)?.getOrNull(col + 1) == '@') {
                    array[row][col]++
                    array[row + 1][col + 1]++
                }
                if (lines.getOrNull(row + 1)?.getOrNull(col) == '@') {
                    array[row][col]++
                    array[row + 1][col]++
                }
                if (lines.getOrNull(row + 1)?.getOrNull(col - 1) == '@') {
                    array[row][col]++
                    array[row + 1][col - 1]++
                }
            } else array[row][col] = -1
        }
    }
    return array
}
