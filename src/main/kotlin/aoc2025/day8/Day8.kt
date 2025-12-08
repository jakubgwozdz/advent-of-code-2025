package aoc2025.day8

import catching
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

typealias Pos3 = Triple<Long, Long, Long>

fun Pos3.distanceTo(other: Pos3) = (this.first - other.first) * (this.first - other.first) +
        (this.second - other.second) * (this.second - other.second) +
        (this.third - other.third) * (this.third - other.third)

fun prepare(data: String): Triple<List<Pos3>, List<Pair<Pos3, Pos3>>, UnionFind<Pos3>> {
    val boxes = data.reader().readLines()
        .map { it.split(",") }.map { (x, y, z) -> Pos3(x.toLong(), y.toLong(), z.toLong()) }

    val connections = boxes.flatMapIndexed { i, b1 -> boxes.subList(0, i).map { b2 -> b1 to b2 } }
        .sortedBy { (b1, b2) -> b1.distanceTo(b2) }.drop(1)

    val indices = boxes.withIndex().associate { (i, triple) -> triple to i }
    val uf = UnionFind<Pos3>(boxes.size) { indices[it]!! }

    return Triple(boxes, connections, uf)
}

fun part1(data: String, conns: Int = 1000): Any {
    val (boxes: List<Pos3>, connections: List<Pair<Pos3, Pos3>>, uf: UnionFind<Pos3>) = prepare(data)

    connections.take(conns).forEach { (b1, b2) -> uf.union(b1, b2) }
    return boxes.groupBy { uf.find(it) }
        .map { it.value.size }
        .sortedDescending().take(3).reduce(Int::times)
}

fun part2(data: String): Any {
    val (boxes: List<Pos3>, connections: List<Pair<Pos3, Pos3>>, uf: UnionFind<Pos3>) = prepare(data)

    var added = 0
    connections.forEach { (b1, b2) ->
        val r1 = uf.find(b1)
        val r2 = uf.find(b2)
        if (r1 != r2) { // kruskal
            uf.union(b1, b2)
            added++
            if (added == boxes.size - 1) return b1.first * b2.first
        }
    }
    error("not gonna happen")
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
