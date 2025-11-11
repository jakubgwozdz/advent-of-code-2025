import org.slf4j.LoggerFactory
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

private val logger = LoggerFactory.getLogger("InputProvider")

fun provideInput(event: Int, day: Int, invalidate: Boolean = false): String =
    cached("local/advent_of_code_${event}/day${day}_input.txt", invalidate) {
        val cookie = File("local/aoc-cookie").readText().trim()
        download("https://adventofcode.com/${event}/day/${day}/input", cookie)
    }

fun download(uri: String, cookie: String? = null) = HttpClient.newHttpClient().send(
    HttpRequest.newBuilder()
        .uri(URI.create(uri))
        .let { if (!cookie.isNullOrBlank()) it.header("Cookie", cookie) else it }
        .build(),
    HttpResponse.BodyHandlers.ofString()
)
    .also { check(it.statusCode() == 200) { "Status code ${it.statusCode()} for $uri" } }
    .body()!!
    .also { logger.info("Downloaded $uri, $") }

fun cached(path: String, invalidate: Boolean = false, block: () -> String): String {
    val file = File(path)
    if (file.canRead() && file.length() > 0) {
        if (invalidate) file.delete()
            .also { logger.info("Invalidated cached $path") }
        else return file.readText()
            .also { logger.info("Loaded cached from $path") }
    }
    return block().also {
        file.parentFile.mkdirs()
        file.writeText(it)
            .also { logger.info("Cached in $path") }
    }
}
