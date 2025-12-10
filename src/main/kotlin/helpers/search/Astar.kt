package helpers.search

import helpers.Direction
import helpers.Pos
import helpers.collections.PriorityQueue
import helpers.manhattanDistance
import helpers.move

// A* search
fun <T> reconstructPath(cameFrom: Map<T, T>, end: T): List<T> {
    val totalPath = mutableListOf(end)
    var current = end
    while (current in cameFrom.keys) {
        current = cameFrom[current]!!
        totalPath.add(0, current)
    }
    return totalPath
}

fun <T : Any> astar(start: T, end: T, heuristics: (T) -> Int, neighbours: (T) -> Iterable<Pair<T, Int>>) =
    astar(start, { it == end }, heuristics, neighbours)

fun <T : Any> astar(
    start: T,
    endPredicate: (T) -> Boolean,
    heuristics: (T) -> Int,
    neighbours: (T) -> Iterable<Pair<T, Int>>
): List<T>? {
    val comeFrom = mutableMapOf<T, T>()
    val gScore = mutableMapOf(start to 0).withDefault { Int.MAX_VALUE / 2 }
    val fScore = mutableMapOf(start to heuristics(start)).withDefault { Int.MAX_VALUE / 2 }
    val toCheck = PriorityQueue({ a, b -> fScore.getValue(a) - fScore.getValue(b) }, start)
    while (toCheck.isNotEmpty()) {
        val current = toCheck.removeFirst()
        if (endPredicate(current)) return reconstructPath(comeFrom, current)
        neighbours(current).forEach { (neighbor, dist) ->
            val tentativeGScore = gScore.getValue(current) + dist
            if (tentativeGScore < gScore.getValue(neighbor)) {
                gScore[neighbor] = tentativeGScore
                fScore[neighbor] = tentativeGScore + heuristics(neighbor)
                comeFrom[neighbor] = current
                toCheck.add(neighbor)
            }
        }
    }
    return null
}

fun astar(start: Pos, end: Pos, walls: Set<Pos>) = astar(start, end, end::manhattanDistance) { current ->
    Direction.entries.map { current.move(it) to 1 }.filterNot { (neighbor) -> neighbor in walls }
}
