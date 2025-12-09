package aoc2025.day9

import catching
import debug
import eventAndDayFromPackage
import go
import helpers.Pos
import helpers.col
import helpers.longPos
import helpers.row
import helpers.x
import helpers.y
import provideInput

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

    // vertices
    val reds = data.reader().readLines().map { it.split(",").let { (a, b) -> Pos(a.toInt(), b.toInt()) } }

    // edges, normalized (lesser..greater)
    val greens = (reds.zipWithNext() + (reds.last() to reds.first())).map { (p1, p2) ->
        if (p1.x == p2.x) (p1.x..p1.x) to (p1.y.coerceAtMost(p2.y)..p1.y.coerceAtLeast(p2.y))
        else if (p1.y == p2.y) (p1.x.coerceAtMost(p2.x)..p1.x.coerceAtLeast(p2.x)) to (p1.y..p1.y)
        else error("impossible")
    }

    // horizontal lines (y to x1..x2)
    val hLines = greens.filter { (p1, p2) -> p1.first != p1.last }
        .map { (xs, ys) -> ys.single() to xs }
        .sortedBy { it.first }

    // vertical lines (x to y1..y2)
    val vLines = greens.filter { (p1, p2) -> p1.first == p1.last }
        .map { (xs, ys) -> xs.single() to ys }
        .sortedBy { it.first }

    val interestingXs = buildSet { reds.forEach { add(it.x); add(it.x - 1); add(it.x + 1) } }
    val interestingYs = buildSet { reds.forEach { add(it.y); add(it.y - 1); add(it.y + 1) } }

    val hLinesFiltered = interestingXs.associateWith { x -> hLines.filterByRanges(x) }
    val vLinesFiltered = interestingYs.associateWith { y -> vLines.filterByRanges(y) }

    val rects = reds.flatMapIndexed { i, p1 ->
        val (x1, y1) = p1
        reds.subList(0, i).map { p2 ->
            val (x2, y2) = p2
            val p1a = Pos(x1.coerceAtMost(x2), y1.coerceAtMost(y2))
            val p2a = Pos(x1.coerceAtLeast(x2), y1.coerceAtLeast(y2))
            Rect(p1a, p2a, (p2a.x - p1a.x + 1L) * (p2a.y - p1a.y + 1L))
        }
    }.filter { (p1, p2) -> p1.x != p2.x && p1.y != p2.y }

    var best = 0L
    var tested = 0

     rects.forEach { rect ->
         if (rect.size > best) {
             tested++
             val minX = rect.p1.x + 1
             val minY = rect.p1.y + 1
             val maxX = rect.p2.x - 1
             val maxY = rect.p2.y - 1

             if (inside(minY, maxY, hLinesFiltered[minX]!!) &&
                     inside(minX, maxX, vLinesFiltered[minY]!!) &&
                     inside(minY, maxY, hLinesFiltered[maxX]!!) &&
                     inside(minX, maxX, vLinesFiltered[maxY]!!)) best = rect.size
         }
    }
    tested.debug()

    return best
}

private fun <V> inside(min: Int, max: Int, filtered: List<Pair<Int, V>>): Boolean {
    val countMin = filtered.binarySearchBy(min) { it.first }.let { if (it < 0) -it - 1 else it }
    return countMin % 2 == 1 && max < filtered[countMin].first
}

private fun <K> List<Pair<K, IntRange>>.filterByRanges(v: Int): List<Pair<K, IntRange>> = filter { (_, range) -> range.first < v && v < range.last }
