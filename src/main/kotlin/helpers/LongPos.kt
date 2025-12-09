package helpers

@JvmInline
value class LongPos(val value: Long) {
    constructor(row: Int, col: Int) : this(longPos(row, col).value)
    operator fun component1() = row
    operator fun component2() = col
    override fun toString() = "$row,$col"
}

fun longPos(row: Int, col: Int) = LongPos((row.toLong() shl 32) or (col.toLong() and 0xFFFF_FFFFL))
val LongPos.row get() = value.shr(32).toInt()
val LongPos.col get() = (value and 0xFFFF_FFFFL).toInt()

// swapped for XY
val LongPos.x get() = row
val LongPos.y get() = col
