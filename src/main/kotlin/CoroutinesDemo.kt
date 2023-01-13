import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlinx.coroutines.*

val logger: Logger = LoggerFactory.getLogger("CoroutinesDemo")

suspend fun bathTime() {
    logger.info("Going to the bathroom")
    delay(500L)
    logger.info("Bath done, exiting")
}

suspend fun main(array: Array<String>) {
    bathTime()
}