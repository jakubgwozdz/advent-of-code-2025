package helpers

// Use to replace HashMap<State, T> with Array<T> (for every possible State)

class Indexer<T>(val bases: List<Int>, val ser: (T) -> IntArray, val deser: (IntArray) -> T) {
    val multipliers = bases
        .asReversed()
        .runningFold(1) { acc, b -> acc * b }.reversed().drop(1)

    fun indexOf(t: T): Int = ser(t).let { ints -> ints.indices.sumOf { ints[it] * multipliers[it] } }
    fun valueOf(index: Int): T {
        var rest = index
        val result = IntArray(bases.size) { c ->
            (rest / multipliers[c]).also { rest %= multipliers[c] }
        }
        return deser(result)
    }
}
