#!/usr/bin/env kotlin

import java.io.File
import java.time.LocalDate
import java.util.Locale

/*
 * Run using
 * ./puzzlefile.main.kts 2024 7
 * to create file for event aoc2024 and puzzle day7
 */

val event = args.getOrElse(0) {LocalDate.now().year}
    .let { "aoc$it" }
    .also { println("Event: $it") }

val puzzle = args.getOrElse(1) {LocalDate.now().dayOfMonth}
    .let { "day$it" }
    .also { println("Puzzle: $it") }

val capitalized = puzzle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

// language="kotlin"
val template = """
    package $event.$puzzle

    import catching
    import debug
    import go
    import eventAndDayFromPackage
    import provideInput

    fun main() = catching {
        val (event, day) = eventAndDayFromPackage { }
        val input = provideInput(event, day)
        go("part 1") { part1(input) }
        go("part 2") { part2(input) }
    }

    fun part1(data: String): Any {
        return data
    }

    fun part2(data: String): Any {
        return data.length to data.lines().size
    }
""".trimIndent()

val fileName = File("src/main/kotlin/$event/$puzzle/$capitalized.kt")
    .also { println("Will create file $it") }

fileName.parentFile.mkdirs()
fileName.writeText(template)
