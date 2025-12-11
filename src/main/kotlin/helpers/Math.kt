package helpers

tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

fun lcm(a: Int, b: Int): Int = a / gcd(a, b) * b
fun lcm(a: Long, b: Long): Long = a / gcd(a, b) * b
