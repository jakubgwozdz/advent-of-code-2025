package aoc2025.day8

import debug
import display
import eventAndDayFromPackage
import provideInput
import shifted
import useGraphics
import withAlpha
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.sqrt

data class AnimState(
    val groups: Map<Int, Iterable<Pos3>>,
    val connections: List<Triple<Pos3, Pos3, Int>> = emptyList(),
    val cycles: List<Triple<Pos3, Pos3, Int>> = emptyList(),
)

fun main() {
    val (event, day) = eventAndDayFromPackage { }
    val input = provideInput(event, day)

//    val (boxes, connections, uf) = prepare(input)
    val (boxes, connections, uf) = prepare(ex1)

    val boundingMin = boxes.reduce { (x1, y1, z1), (x2, y2, z2) -> Pos3(minOf(x1, x2), minOf(y1, y2), minOf(z1, z2)) }
        .debug { "bounding box min: $it" }
    val boundingMax = boxes.reduce { (x1, y1, z1), (x2, y2, z2) -> Pos3(maxOf(x1, x2), maxOf(y1, y2), maxOf(z1, z2)) }
        .debug { "bounding box max: $it" }

    val anim = AtomicReference(AnimState(boxes.groupBy(uf::find)))
    val video = Day8Video(boundingMin, boundingMax, boxes.size)

    display(anim, "Day 8: Playground", dimension = Dimension(600, 600), op = video::paintOnImage)

    repeat(10) {
        println(10 - it)
        sleep(1000)
    }

    var added = 0
    connections.forEach { (b1, b2) ->
        val r1 = uf.find(b1)
        val r2 = uf.find(b2)
        if (r1 != r2) { // kruskal
            uf.union(b1, b2)
            added++
            added.debug { "added $added of ${boxes.size} boxes" }
            anim.updateAndGet {
                it.copy(
                    groups = boxes.groupBy(uf::find),
                    cycles = it.cycles.map { it.copy(third = uf.find(it.first)) },
                    connections = it.connections.map { it.copy(third = uf.find(it.first)) } + Triple(b1, b2, uf.find(b1))
                )
            }
        } else if (added < boxes.size - 1) {
            anim.updateAndGet {
                it.copy(cycles = it.cycles + Triple(b1, b2, uf.find(b1)))
            }
        }
        sleep(20000L / boxes.size)
    }
}

data class Bounds(val minX: Double, val maxX: Double, val minY: Double, val maxY: Double)

class Day8Video(val boundingMin: Pos3, val boundingMax: Pos3, val count: Int) {
    val bgColor = Color(0x443742).darker().withAlpha(180)
    val lastColor = Color(0xF3DFA2).withAlpha(100)

    val center = Pos3(
        (boundingMax.first - boundingMin.first) / 2,
        (boundingMax.second - boundingMin.second) / 2,
        (boundingMax.third - boundingMin.third) / 2
    )

    val camera0 = Pos3(center.first, - center.second, center.third)
    val bounds = calculateBounds(boundingMin, boundingMax, center, camera0)
        .debug()

    fun paintOnImage(state: AnimState, image: BufferedImage) = image.useGraphics { g ->
        g.color = bgColor//.withAlpha(200)
        g.fillRect(0, 0, image.width, image.height)
        g.stroke = BasicStroke(5.1f)

        val angle = Math.toRadians(System.currentTimeMillis() / 50.0)
        val dx = camera0.first - center.first
        val dy = camera0.second - center.second

        val camera = Pos3(
            (center.first + dx * cos(angle) - dy * sin(angle)).roundToLong(),
            (center.second + dx * sin(angle) + dy * cos(angle)).roundToLong(),
            camera0.third
        )

        state.groups.forEach { (index, group) ->
            g.color = lastColor.shifted(index * -1.0f / count)
            group.forEach { pos3 ->
                val posDouble = get2D(pos3, camera, center)
                    ?: return@forEach
                val pos = posDouble.scaleToImage(bounds, image.width, image.height)
                val size = 18.0
                g.fill(Ellipse2D.Double(pos.first - size / 2, pos.second - size / 2, size, size))
                if (state.groups.size == 1) {
                    g.stroke = BasicStroke(3.1f)
                    g.draw(Ellipse2D.Double(pos.first - size / 2, pos.second - size / 2, size, size))
                }
            }
        }

        g.stroke = BasicStroke(5.1f)

        state.connections.forEach { (b1, b2, index) ->
            g.color = lastColor.shifted(index * 1.0f / count)
            val pos1 = get2D(b1, camera, center)?.scaleToImage(bounds, image.width, image.height)
                ?: return@forEach
            val pos2 = get2D(b2, camera, center)?.scaleToImage(bounds, image.width, image.height)
                ?: return@forEach
            g.drawLine(pos1.first.toInt(), pos1.second.toInt(), pos2.first.toInt(), pos2.second.toInt())
            if (state.groups.size == 1) {
                g.stroke = BasicStroke(7.1f)
                g.drawLine(pos1.first.toInt(), pos1.second.toInt(), pos2.first.toInt(), pos2.second.toInt())
            }
        }

//        g.stroke =
//            BasicStroke(1.1f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, floatArrayOf(4f, 6f), 0.0f)
        g.stroke = BasicStroke(1f)

        state.cycles.forEach { (b1, b2, index) ->
            g.color = lastColor.shifted(index * 1.0f / count).darker()
            val pos1 = get2D(b1, camera, center)?.scaleToImage(bounds, image.width, image.height)
                ?: return@forEach
            val pos2 = get2D(b2, camera, center)?.scaleToImage(bounds, image.width, image.height)
                ?: return@forEach
            g.drawLine(pos1.first.toInt(), pos1.second.toInt(), pos2.first.toInt(), pos2.second.toInt())
        }

    }

}

fun get2D(pos3: Pos3, camera: Pos3, center: Pos3): Pair<Double, Double>? {
    val dirX = center.first - camera.first.toDouble()
    val dirY = center.second - camera.second.toDouble()
    val dirZ = center.third - camera.third.toDouble()
    val dirLen = sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ)

    val fwdX = dirX / dirLen
    val fwdY = dirY / dirLen
    val fwdZ = dirZ / dirLen

    val rightX = fwdY
    val rightY = -fwdX
    val rightZ = 0.0
    val rightLen = sqrt(rightX * rightX + rightY * rightY + rightZ * rightZ)
    val rX = if (rightLen > 0) rightX / rightLen else 1.0
    val rY = if (rightLen > 0) rightY / rightLen else 0.0
    val rZ = if (rightLen > 0) rightZ / rightLen else 0.0

    val upX = rY * fwdZ - rZ * fwdY
    val upY = rZ * fwdX - rX * fwdZ
    val upZ = rX * fwdY - rY * fwdX

    val px = pos3.first - camera.first
    val py = pos3.second - camera.second
    val pz = pos3.third - camera.third

    val depth = px * fwdX + py * fwdY + pz * fwdZ
    val x = px * rX + py * rY + pz * rZ
    val y = px * upX + py * upY + pz * upZ

    return if (depth > 0.1) {
        x / depth to y / depth
    } else {
        null
    }
}

fun calculateBounds(boundingMin: Pos3, boundingMax: Pos3, center: Pos3, camera: Pos3): Bounds {
    val (x1, y1, z1) = boundingMin
    val (x2, y2, z2) = boundingMax

    val points = listOf(
        Pos3(x1, y1, z1),
        Pos3(x1, y1, z2),
        Pos3(x1, y2, z1),
        Pos3(x1, y2, z2),
        Pos3(x2, y1, z1),
        Pos3(x2, y1, z2),
        Pos3(x2, y2, z1),
        Pos3(x2, y2, z2),
    ).map { get2D(it, camera, center)!! }
    return Bounds(
        points.minOf { it.first }, points.maxOf { it.first },
        points.minOf { it.second }, points.maxOf { it.second }
    )
}

fun Pair<Double, Double>.scaleToImage(bounds: Bounds, width: Int, height: Int): Pair<Double, Double> {
    val margin = 40.0
    val targetWidth = width - 2 * margin
    val targetHeight = height - 2 * margin

    val boundsWidth = bounds.maxX - bounds.minX
    val boundsHeight = bounds.maxY - bounds.minY

    val scaleX = targetWidth / boundsWidth
    val scaleY = targetHeight / boundsHeight
    val scale = minOf(scaleX, scaleY)

    val scaledWidth = boundsWidth * scale
    val scaledHeight = boundsHeight * scale

    val offsetX = margin + (targetWidth - scaledWidth) / 2
    val offsetY = margin + (targetHeight - scaledHeight) / 2

    val x = offsetX + (first - bounds.minX) * scale
    val y = offsetY + (second - bounds.minY) * scale

    return Pair(x, y)
}
