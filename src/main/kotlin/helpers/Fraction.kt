package helpers

import kotlin.math.abs

data class Fraction(val n: Long, val d: Long = 1) : Comparable<Fraction> {
    init {
        require(d != 0L) { "Division by zero" }
    }

    val num: Long
    val den: Long

    init {
        val common = gcd(abs(n), abs(d))
        if (d < 0) {
            num = -n / common
            den = -d / common
        } else {
            num = n / common
            den = d / common
        }
    }

    operator fun plus(other: Fraction) = Fraction(num * other.den + other.num * den, den * other.den)
    operator fun minus(other: Fraction) = Fraction(num * other.den - other.num * den, den * other.den)
    operator fun times(other: Fraction) = Fraction(num * other.num, den * other.den)
    operator fun div(other: Fraction) = Fraction(num * other.den, den * other.num)
    operator fun unaryMinus() = Fraction(-num, den)

    fun isInteger() = den == 1L
    fun toLong() = num / den
    fun isNegative() = num < 0

    override fun compareTo(other: Fraction): Int = (num * other.den).compareTo(other.num * den)
    override fun toString() = if (den == 1L) "$num" else "$num/$den"

    companion object {
        val ZERO = Fraction(0)
        val ONE = Fraction(1)
        fun from(b: Boolean) = if (b) ONE else ZERO
        fun from(i: Int) = Fraction(i.toLong())
    }
}
