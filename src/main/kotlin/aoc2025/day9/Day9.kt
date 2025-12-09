package aoc2025.day9

import catching
import eventAndDayFromPackage
import go
import helpers.Pos
import helpers.col
import helpers.longPos
import helpers.row
import helpers.x
import helpers.y
import provideInput
import kotlin.math.max
import kotlin.math.min

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)
    val ex = """
        7,1
        11,1
        11,7
        9,7
        9,5
        2,5
        2,3
        7,3
    """.trimIndent()
    go("ex1", 50) { part1(ex) }
    go("part 1", 4763932976) { part1(input) }
    go("ex1", 24) { part2(ex) }
    go("part 2", 1501292304) { part2(input) }
}

fun part1(data: String): Any {
    val reds = data.reader().readLines().map { it.split(",").let { (a, b) -> longPos(a.toInt(), b.toInt()) } }
    return reds.flatMapIndexed { i, p1 ->
        reds.subList(0, i).map { p2 ->
            val minRow = p1.row.coerceAtMost(p2.row)
            val maxRow = p1.row.coerceAtLeast(p2.row)
            val minCol = p1.col.coerceAtMost(p2.col)
            val maxCol = p1.col.coerceAtLeast(p2.col)
            (maxRow - minRow + 1L) * (maxCol - minCol + 1L)
        }
    }.max()
}

data class Rect(
    val p1: Pos,
    val p2: Pos,
    val size: Long
)

fun part2(data: String): Any {
    val reds = data.reader().readLines().map { it.split(",").let { (a, b) -> Pos(a.toInt(), b.toInt()) } }
    val greens = (reds.zipWithNext() + (reds.last() to reds.first())).map { (p1, p2) ->
        if (p1.x == p2.x) (p1.x..p1.x) to (p1.y.coerceAtMost(p2.y)..p1.y.coerceAtLeast(p2.y))
        else if (p1.y == p2.y) (p1.x.coerceAtMost(p2.x)..p1.x.coerceAtLeast(p2.x)) to (p1.y..p1.y)
        else error("impossible")
    }

    val hLines = greens.filter { (p1, p2) -> p1.first != p1.last }
        .map { (xs, ys) -> ys.single() to xs }

    val vLines = greens.filter { (p1, p2) -> p1.first == p1.last }
        .map { (xs, ys) -> xs.single() to ys }

    val rects = reds.flatMapIndexed { i, p1 ->
        val (x1, y1) = p1
        reds.subList(0, i).map { p2 ->
            val (x2, y2) = p2
            val p1a = Pos(x1.coerceAtMost(x2), y1.coerceAtMost(y2))
            val p2a = Pos(x1.coerceAtLeast(x2), y1.coerceAtLeast(y2))
            Rect(p1a, p2a, (p2a.x - p1a.x + 1L) * (p2a.y - p1a.y + 1L))
        }
    }.filter { (p1, p2) -> p1.x != p2.x && p1.y != p2.y }

    return rects.filter { rect ->
        val minX = rect.p1.x + 1
        val minY = rect.p1.y + 1
        val maxX = rect.p2.x - 1
        val maxY = rect.p2.y - 1

        if (!valid(hLines, minX, minY, maxY)) return@filter false
        if (!valid(hLines, maxX, minY, maxY)) return@filter false
        if (!valid(vLines, minY, minX, maxX)) return@filter false
        if (!valid(vLines, maxY, minX, maxX)) return@filter false

        true
    }.maxOf { it.size }
}

private fun valid(lines: List<Pair<Int, IntRange>>, main: Int, otherMin: Int, otherMax: Int): Boolean {
    val filtered = lines.filter { (_, range) -> range.first < main && main < range.last }
    val countMinY1 = filtered.count { (r, _) -> r < otherMin }
    val countMaxY1 = filtered.count { (r, _) -> r < otherMax }
    return countMinY1 % 2 == 1 && countMaxY1 == countMinY1
}
