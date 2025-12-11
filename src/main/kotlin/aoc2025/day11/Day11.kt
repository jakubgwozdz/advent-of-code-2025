package aoc2025.day11

import catching
import go
import eventAndDayFromPackage
import provideInput
import kotlin.collections.orEmpty

fun main() = catching {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)

    val ex = """
        aaa: you hhh
        you: bbb ccc
        bbb: ddd eee
        ccc: ddd eee fff
        ddd: ggg
        eee: out
        fff: out
        ggg: out
        hhh: ccc fff iii
        iii: out
    """.trimIndent()

    go("part 1 ex", 5) { part1(ex) }
    go("part 1") { part1(input) }
    go("part 2") { part2(input) }
}

fun dfs(end: String, start: String, graph: Map<String, List<String>>): Long {
    val cache = mutableMapOf<String, Long>()
    fun search(id: String): Long = graph[id].orEmpty().sumOf { i ->
        if (i == start) 1 else cache.getOrPut(i) { search(i) }
    }
    return search(end)
}

fun part1(data: String): Any {
    val inputs = outputs(data)
    return dfs("you", "out", inputs)
}

fun part2(data: String): Any {
    val inputs = outputs(data)
    return dfs("svr", "fft", inputs) * dfs("fft", "dac", inputs) * dfs("dac", "out", inputs) +
            dfs("svr", "dac", inputs) * dfs("dac", "fft", inputs) * dfs("fft", "out", inputs)

}

private fun outputs(data: String) = data.reader().readLines().associate {
    it.split(":")
        .let { (device, outputs) -> device to outputs.split(' ').map(String::trim).filterNot(String::isBlank) }
}
