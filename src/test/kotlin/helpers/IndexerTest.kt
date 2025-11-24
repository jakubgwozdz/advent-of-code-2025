package helpers

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IndexerTest {

    @Test
    fun testIndexing() {
        data class MyClass(
            val aBetweenNeg5And5: Int,
            val bBetweenCAndF: Char,
            val cBoolean: Boolean,
            val dBetween1And52: Int,
        )

        val aRange = (-5..5).toList()
        val bRange = ('C'..'F').toList()
        val cRange = listOf(false, true)
        val dRange = (1..52).toList()

        fun serialize(value: MyClass) = intArrayOf(
            value.aBetweenNeg5And5 + 5,
            value.bBetweenCAndF - 'C',
            if (value.cBoolean) 1 else 0,
            value.dBetween1And52 - 1,
        )

        fun deserialize(index: IntArray) = MyClass(
            aRange[index[0]],
            bRange[index[1]],
            cRange[index[2]],
            dRange[index[3]],
        )

        val indexer = Indexer(
            listOf(aRange.size, bRange.size, cRange.size, dRange.size),
            ::serialize, ::deserialize
        )

        assertEquals(52 * 2 * 4 * 11, indexer.bases.reduce(Int::times))

        aRange.forEach { a ->
            bRange.forEach { b ->
                cRange.forEach { c ->
                    dRange.forEach { d ->
                        val value = MyClass(a, b, c, d)
                        val index = indexer.indexOf(value)
                        assertEquals(value, indexer.valueOf(index))
                    }
                }
            }
        }
    }

}