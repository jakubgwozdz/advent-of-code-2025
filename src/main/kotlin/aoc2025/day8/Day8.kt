package aoc2025.day8

import catching
import debug
import go
import eventAndDayFromPackage
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)

    val ex1 = """
        162,817,812
        57,618,57
        906,360,560
        592,479,940
        352,342,300
        466,668,158
        542,29,236
        431,825,988
        739,650,466
        52,470,668
        216,146,977
        819,987,18
        117,168,530
        805,96,715
        346,949,466
        970,615,88
        941,993,340
        862,61,35
        984,92,344
        425,690,689
    """.trimIndent()

    go("part 1 ex1", 40) { part1(ex1, 10) }
    go("part 1", 352584) { part1(input) }
    go("part 2 ex1", 25272) { part2(ex1) }
    go("part 2", 9617397716) { part2(input) }
}

typealias Box = Triple<Long, Long, Long>
typealias Connection = Triple<Box, Box, Long>

operator fun Box.compareTo(other: Box) = compareValuesBy(this, other, Box::first, Box::second, Box::third)

fun Box.distanceTo(other: Box) = (this.first - other.first) * (this.first - other.first) +
        (this.second - other.second) * (this.second - other.second) +
        (this.third - other.third) * (this.third - other.third)

fun solve(data: String, op: (boxes: List<Box>, connections: List<Connection>, uf: UnionFind<Box>) -> Any): Any {
    val boxes = data.reader().readLines()
        .map { it.split(",") }.map { (x, y, z) -> Box(x.toLong(), y.toLong(), z.toLong()) }

    val connections = boxes.flatMap { b1 -> boxes.map { b2 -> Connection(b1, b2, b1.distanceTo(b2)) } }
        .filter { it.first < it.second }
        .sortedBy { it.third }

    val indices = boxes.withIndex().associate { (i, triple) -> triple to i }

    val uf = UnionFind<Box>(boxes.size) { indices[it]!! }

    return op(boxes, connections, uf)
}

fun part1(data: String, conns: Int = 1000) = solve(data) { boxes, connections, uf ->
    connections.take(conns).forEach { (b1, b2, _) -> uf.union(b1, b2) }
    boxes.groupBy { uf.find(it) }
        .map { it.value.size }
        .sortedDescending().take(3).reduce(Int::times)
}

fun part2(data: String) = solve(data) { boxes, connections, uf ->
    repeat(connections.size) {
        val (b1, b2, _) = connections[it]
        uf.union(b1, b2)
        val p1 = uf.find(b1)
        if (boxes.all { uf.find(it) == p1 }) return@solve b1.first * b2.first
    }
    error("should not be reached")
}

class UnionFind<T>(size: Int, val indexOp: (T) -> Int) {
    private val parent = IntArray(size) { it }
    private val rank = IntArray(size) { 0 }

    fun findByIndex(i: Int): Int {
        if (parent[i] != i) parent[i] = findByIndex(parent[i])
        return parent[i]
    }

    fun find(e: T): Int = findByIndex(indexOp(e))

    fun unionByIndex(i1: Int, i2: Int) {
        val rootX = findByIndex(i1)
        val rootY = findByIndex(i2)
        if (rootX != rootY) when {
            rank[rootX] > rank[rootY] -> parent[rootY] = rootX
            rank[rootX] < rank[rootY] -> parent[rootX] = rootY
            else -> parent[rootY] = rootX.also { rank[rootX]++ }
        }
    }

    fun union(e1: T, e2: T) = unionByIndex(indexOp(e1), indexOp(e2))
}
