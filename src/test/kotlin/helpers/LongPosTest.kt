package helpers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource

class LongPosTest {
    @ParameterizedTest
    @FieldSource
    fun testLongPos(pair: Pair<Int, Int>) {
        val pos = longPos(pair.first, pair.second)
        println(pos)
        assertEquals(pair, pos.row to pos.col)
    }

    companion object {
        val testLongPos = listOf(
            0 to 0,
            1 to 2,
            -2 to -1,
            -1 to 4,
            5 to -6,
            Int.MAX_VALUE to Int.MAX_VALUE,
            Int.MAX_VALUE to Int.MIN_VALUE,
            Int.MIN_VALUE to Int.MAX_VALUE,
            Int.MIN_VALUE to Int.MIN_VALUE,
        )
    }

}