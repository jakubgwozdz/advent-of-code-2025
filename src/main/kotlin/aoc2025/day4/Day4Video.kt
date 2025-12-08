package aoc2025.day4

import display
import eventAndDayFromPackage
import helpers.LongPos
import helpers.Pos
import helpers.col
import helpers.longPos
import helpers.n
import helpers.neighbours8
import helpers.row
import provideInput
import useGraphics
import withAlpha
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference

data class AnimState(
    val arrays: List<IntArray>,
    val result: Int = 0,
    val lastChecked: Pos = Pos(-1, -1),
)

fun main() {
    val (event, day) = eventAndDayFromPackage { }
    val arrays = parse(provideInput(event, day))
    val anim = AtomicReference(AnimState(arrays))
    val video = Day4Video(arrays.indices, arrays.first().indices)

    display(anim, "Day 4: Printing Department", dimension = Dimension(600, 600), op = video::paintOnImage)

    val removed = mutableListOf<LongPos>()
    var result = 0

    arrays.forEachIndexed { row, line ->
        line.forEachIndexed { col, v ->
            if (v in 0..<4) removed.add(longPos(row, col)).also { arrays[row][col] = -1 }
        }
    }

    repeat(10) {
        println(10 - it)
        sleep(1000)
    }

    while (removed.isNotEmpty()) {
        val pos = removed.removeLast()
        result++
        anim.getAndUpdate { it.copy(result = result, lastChecked = pos) }
        sleep(3)
        pos.neighbours8().forEach { p1 ->
            val (r1, c1) = p1
            if (r1 in arrays.indices && c1 in arrays[r1].indices && arrays[r1][c1] > 3) {
                arrays[r1][c1]--
                if (arrays[r1][c1] == 3) removed.add(p1).also { arrays[r1][c1] = -1 }
            }
        }
    }
    anim.getAndUpdate { it.copy(result = result, lastChecked = Pos(-1, -1)) }


}

class Day4Video(val rowRange: IntRange, val colRange: IntRange) {
    val bgColor = Color(0x443742).withAlpha(80)
    val leftColor = Color(0x03CEA4)
    val rightColor = Color(0xFB4D3D)
    val otherColor = Color(0x285943)
    val lastColor = Color(0xF3DFA2)//.withAlpha(50)

    fun paintOnImage(state: AnimState, image: BufferedImage) = image.useGraphics { g ->
        g.color = bgColor//.withAlpha(200)
        g.fillRect(0, 0, image.width, image.height)
        g.scale(
            image.width / (colRange.last - colRange.first + 4.0),
            image.width / (rowRange.last - rowRange.first + 4.0)
        )
        g.translate(2.0, 2.0)
        g.stroke = BasicStroke(0.1f)
        g.color = lastColor

        state.arrays.forEachIndexed { row, line ->
            line.forEachIndexed { col, v ->
                if (v >= 0) {
                    g.drawBrick(longPos(row, col))
                }
            }
        }

//        g.color = lastColor
//        g.drawBrick(state.lastChecked, size = 1.0)
    }

    private fun Graphics2D.drawBrick(pos: Pos, size: Double = 0.6) =
        fill(Ellipse2D.Double(pos.col - size / 2, pos.row - size / 2, size, size))

}
