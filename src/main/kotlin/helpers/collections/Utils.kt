package helpers.collections

fun <E> List<List<E>>.transposed(defaultOp: (Int) -> E = { error("No default provided") }): List<List<E>> {
    return first().indices.map { j -> map { it.getOrElse(j, defaultOp) } }
}

fun <T> MutableMap<T, Int>.increment(k: T, i: Int = 1) =
    put(k, getOrDefault(k, 0) + i)

fun <S, T> MutableMap<S, MutableMap<T, Int>>.increment(k1: S, k2: T, i: Int = 1) =
    getOrPut(k1) { mutableMapOf() }.increment(k2, i)

fun <T> MutableMap<T, Long>.increment(k: T, i: Long = 1) =
    put(k, getOrDefault(k, 0) + i)

fun <S, T> MutableMap<S, MutableMap<T, Long>>.increment(k1: S, k2: T, i: Long = 1) =
    getOrPut(k1) { mutableMapOf() }.increment(k2, i)
