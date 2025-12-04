package helpers

import kotlin.math.abs

typealias Pos = LongPos

fun List<CharSequence>.findAll(ch: Char): Set<Pos> = flatMapIndexed { r, line ->
    line.mapIndexedNotNull { c, ch1 -> if (ch1 == ch) Pos(r, c) else null }
}.toSet()

fun List<CharSequence>.groupByChar(): Map<Char, Set<Pos>> = flatMapIndexed { r, line ->
    line.mapIndexedNotNull { c, ch -> Pos(r, c) to ch }
}.groupBy({ it.second }, { it.first }).mapValues { it.value.toSet() }

operator fun List<CharSequence>.get(pos: Pos): Char? = getOrNull(pos.row)?.getOrNull(pos.col)

fun Pos.n(dist: Int = 1): Pos = Pos(row - dist, col)
fun Pos.ne(): Pos = Pos(row - 1, col + 1)
fun Pos.e(dist: Int = 1): Pos = Pos(row, col + dist)
fun Pos.se(): Pos = Pos(row + 1, col + 1)
fun Pos.s(dist: Int = 1): Pos = Pos(row + dist, col)
fun Pos.sw(): Pos = Pos(row + 1, col - 1)
fun Pos.w(dist: Int = 1): Pos = Pos(row, col - dist)
fun Pos.nw(): Pos = Pos(row - 1, col - 1)

fun LongPos.neighbours8(): Iterable<Pos> =
    listOf(n(), e(), s(), w(), ne(), se(), sw(), nw())

enum class Direction {
    N, E, S, W;

    fun opposite(): Direction = when (this) {
        N -> S
        E -> W
        S -> N
        W -> E
    }

    fun turnRight(): Direction = when (this) {
        N -> E
        E -> S
        S -> W
        W -> N
    }

    fun turnLeft(): Direction = when (this) {
        N -> W
        E -> N
        S -> E
        W -> S
    }
}

fun Pos.move(dir: Direction, dist: Int = 1): Pos {
    return when (dir) {
        Direction.N -> n(dist)
        Direction.E -> e(dist)
        Direction.S -> s(dist)
        Direction.W -> w(dist)
    }
}

fun Pos.manhattanDistance(other: Pos) = abs(row - other.row) + abs(col - other.col)

