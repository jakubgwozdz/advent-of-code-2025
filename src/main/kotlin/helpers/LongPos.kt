package helpers

@JvmInline
value class LongPos(val value: Long) {
    override fun toString() = "$row,$col"
}

fun longPos(row: Int, col: Int) = LongPos((row.toLong() shl 32) or (col.toLong() and 0xFFFF_FFFFL))
val LongPos.row get() = value.shr(32).toInt()
val LongPos.col get() = (value and 0xFFFF_FFFFL).toInt()
