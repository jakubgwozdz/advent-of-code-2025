package aoc2025.day12

import catching
import debug
import go
import eventAndDayFromPackage
import provideInput

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)

    val example = """
        0:
        ###
        ##.
        ##.

        1:
        ###
        ##.
        .##

        2:
        .##
        ###
        ##.

        3:
        ##.
        ###
        ##.

        4:
        ###
        #..
        ###

        5:
        ###
        .#.
        ###

        4x4: 0 0 0 0 2 0
        12x5: 1 0 1 0 2 2
        12x5: 1 0 1 0 3 2
    """.trimIndent()

//    go("part 1 ex", 2) { part1(example) }
    go("part 1") { part1(input) }
    go("part 2") { part2(input) }
}

data class ShapeCase(val rows: List<String>)

data class Shape(val rows: List<String>) {
    init {
        require(rows.size == 3)
        require(rows.all { it.length == 3 })
    }

    val size = rows.sumOf { row -> row.count { it == '#' } }
    val possibilities = buildList<ShapeCase> {
        fun rotateLastAndAdd() {
            val last = last().rows
            val rotated = List(last[0].length) { col ->
                CharArray(last.size) { row ->
                    last[last.size - 1 - row][col]
                }.concatToString()
            }
            add(ShapeCase(rotated))
        }
        add(ShapeCase(rows))
        repeat(3) { rotateLastAndAdd() }
        add(ShapeCase(last().rows.reversed()))
        repeat(3) { rotateLastAndAdd() }
    }.distinct()

    override fun toString(): String =
        rows.indices.joinToString("\n", prefix = "size=$size, ${possibilities.size} possibilities\n") { r ->
            possibilities.joinToString("   ") { it.rows[r] }
        }
}

data class Region(val w: Int, val h: Int, val counts: List<Int>) {
    fun pack(shapes: List<Shape>): Boolean {
        if (counts.sum() <= (w / 3) * (h / 3)) return true
            .debug { "Trivially fits by count" }
        if (shapes.zip(counts).sumOf { (s, n) -> n * s.size } > h * w) return false
            .debug { "Too much area needed" }

        val toGo = shapes.flatMapIndexed { index, shape -> List(counts[index]) { shape } }
            .debug { "To go: ${it.size} shapes" }
        TODO("Implement packing algorithm")
    }
}

data class Setup(val shapes: List<Shape>, val regions: List<Region>) {
    init {
        require(shapes.size == 6)
    }
}

fun part1(data: String): Any {
    val (shapes, regions) = parse(data)
    return regions.count { region -> region.pack(shapes) }
}

private fun parse(data: String): Setup {
    val regionRegex = """(\d+)x(\d+):(( \d+){6})""".toRegex()
    val sections = data.split("\n\n")
    val shapes = mutableListOf<Shape>()
    val regions = mutableListOf<Region>()

    sections.forEach { section ->
        val lines = section.reader().readLines()
        if (lines.first().matches("""\d+:""".toRegex())) {
            val shape = lines.drop(1)
            shapes.add(Shape(shape).debug { "Added shape: $it" })
        } else {
            if (lines.first().matches(regionRegex)) {
                lines.forEach { line ->
                    val match = regionRegex.matchEntire(line) ?: error("Invalid region line: $line")
                    val (w, h, countsStr) = match.destructured
                    val counts = countsStr.trim().split(" ").map { it.toInt() }
                    regions.add(Region(w.toInt(), h.toInt(), counts))
                }
            } else error("Unknown section format: ${lines.first()}")
        }
    }
    return Setup(shapes, regions)
}

fun part2(data: String): Any {
    return data.length to data.lines().size
}
