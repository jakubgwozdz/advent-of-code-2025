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
    val xInside: IntRange,
    val yInside: IntRange,
    val size: Long
)

fun part2(data: String): Any {
    val reds = data.reader().readLines().map { it.split(",").let { (a, b) -> Pos(a.toInt(), b.toInt()) } }
    val (hLines, vLines) = (reds.zipWithNext() + (reds.last() to reds.first()))
        .map { (p1, p2) ->
            if (p1.x == p2.x) (p1.x .. p1.x) to (p1.y.coerceAtMost(p2.y)..p1.y.coerceAtLeast(p2.y))
            else if (p1.y == p2.y) (p1.x.coerceAtMost(p2.x) .. p1.x.coerceAtLeast(p2.x)) to (p1.y .. p1.y)
            else error("impossible")
        }
        .partition { (p1, p2) -> p1.first != p1.last }

    val rects = reds.flatMapIndexed { i, p1 ->
        val (x1, y1) = p1
        reds.subList(0, i).map { p2 ->
            val (x2, y2) = p2
            val xs = x1.coerceAtMost(x2) + 1..x1.coerceAtLeast(x2) - 1
            val ys = y1.coerceAtMost(y2) + 1..y1.coerceAtLeast(y2) - 1
            Rect(p1, p2, xs, ys, (xs.last - xs.first + 3L) * (ys.last - ys.first + 3L))
        }
    }.filter { (p1, p2) -> p1.x != p2.x && p1.y != p2.y }

    return rects.filter { rect ->
        val minX = min(rect.p1.x, rect.p2.x) + 1
        val minY = min(rect.p1.y, rect.p2.y) + 1
        val maxX = max(rect.p1.x, rect.p2.x) - 1
        val maxY = max(rect.p1.y, rect.p2.y) - 1

        val hBorders1 = hLines.filter { (range, _) -> range.first < minX && minX < range.last }
        val countMinY1 = hBorders1.count { (_, range) -> range.first < minY }
        val countMaxY1 = hBorders1.count { (_, range) -> range.first < maxY }
        if (countMinY1 % 2 != 1 || countMaxY1 != countMinY1) return@filter false

        val hBorders2 = hLines.filter { (range, _) -> range.first < maxX && maxX < range.last }
        val countMinY2 = hBorders2.count { (_, range) -> range.first < minY }
        val countMaxY2 = hBorders2.count { (_, range) -> range.first < maxY }
        if (countMinY2 % 2 != 1 || countMaxY2 != countMinY2) return@filter false

        val vBorders1 = vLines.filter { (_, range) -> range.first < minY && minY < range.last }
        val countMinX1 = vBorders1.count { (range, _) -> range.first < minX }
        val countMaxX1 = vBorders1.count { (range, _) -> range.first < maxX }
        if (countMinX1 % 2 != 1 || countMaxX1 != countMinX1) return@filter false

        val vBorders2 = vLines.filter { (_, range) -> range.first < maxY && maxY < range.last }
        val countMinX2 = vBorders2.count { (range, _) -> range.first < minX }
        val countMaxX2 = vBorders2.count { (range, _) -> range.first < maxX }
        if (countMinX2 % 2 != 1 || countMaxX2 != countMinX2) return@filter false

        true
    }.maxOf { it.size }
}
