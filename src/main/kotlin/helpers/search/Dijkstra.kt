package helpers.search

import helpers.collections.PriorityQueue

fun <T : Any> dijkstra(
    start: T,
    endPredicate: (T) -> Boolean,
    priority: Comparator<Pair<T, Int>> = compareBy { it.second },
    neighbours: (T) -> Iterable<Pair<T, Int>>,
): Int {
    val queue = PriorityQueue(priority).apply { add(start to 0) }
    val dist = mutableMapOf(start to 0)

    while (queue.isNotEmpty()) {
        val (current, steps) = queue.removeFirst()
        if (endPredicate(current)) return steps
        neighbours(current)
            .filter { (n, c) -> steps + c < (dist[n] ?: Int.MAX_VALUE) }
            .forEach { (n, c) ->
                dist[n] = steps + c
                queue.add(n to steps + c)
            }
    }
    error("No path found")
}

fun <T : Any> dijkstra(start: T, end: T, neighbours: (T) -> Iterable<Pair<T, Int>>) =
    dijkstra(start, { it == end }, neighbours = neighbours)
