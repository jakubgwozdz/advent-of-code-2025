import org.slf4j.LoggerFactory
import kotlin.time.measureTimedValue

private val logger = LoggerFactory.getLogger("Tester")

internal inline fun <T> go(desc: String, expected: T? = null, op: () -> T) {
    val (result, time) = measureTimedValue { op().toString() }
    logger.info("$desc took $time: ${if ('\n' in result) "\n$result" else result}")
    if (expected != null) check(result == expected.toString()) { "$desc: expected $expected, got $result" }
}

internal fun eventAndDayFromPackage(x: Any): Pair<Int, Int> = x.javaClass.packageName.run {
    check(contains("aoc") && contains("day")) { "package name `$this` for $x must contain 'aoc' and 'day'" }
    substringAfter("aoc").substringBefore(".").toInt() to substringAfter("day").substringBefore(".").toInt()
}.debug { "eventAndDayFromPackage($x) = $it" }

// this is only, so the main does not end with an error causing intellij to scroll output
inline fun catching(block: () -> Unit) = try { block() } catch (e: Throwable) { e.printStackTrace() }
