import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

private val logger = LoggerFactory.getLogger("")

private val delays = mutableMapOf<String, Instant>()

fun debug(msg: Any?) = msg.toString().let {
    logger.debug(if ('\n' in it) "\n$it" else it)
}

fun debug(delay: Duration? = null, op: () -> Any) {
    val key = op.javaClass.toString()
    if (delay != null) {
        val now = Instant.now()
        val last = delays[key]
        if (last != null) {
            val elapsed = Duration.between(last, now)
            if (elapsed < delay) return
        }
        delays[key] = now
    }
    debug(op().toString())
}

inline fun <T> T.debug(crossinline op: (T) -> Any? = { it }): T = also { debug(op(it)) }
