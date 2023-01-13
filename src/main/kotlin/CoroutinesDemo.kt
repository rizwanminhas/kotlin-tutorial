import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlinx.coroutines.*

val logger: Logger = LoggerFactory.getLogger("CoroutinesDemo")

suspend fun bathTime() {
    logger.info("Going to the bathroom")
    delay(500L) // suspends/"blocks" i.e. frees up this thread and the rest of the code is run on some other thread
    logger.info("Bath done, exiting")
}

suspend fun boilingWatter() {
    logger.info("Boiling water")
    delay(1000)
    logger.info("Water boiled")
}

suspend fun sequentialMorningRoutine() {
    // inside a coroutineScope all functions are executed in order
    coroutineScope { // wrapper over suspend to start a context for coroutines
        bathTime()
        // add more code, including suspend functions
        // parallel code here, all needs to finish before the scope is closed
    }
    coroutineScope {
        boilingWatter()
    }
    /*
        this is same as above code
        coroutineScope {
            bathTime()
            boilingWatter()
        }
     */
}

suspend fun concurrentMorningRoutine() {
    // launch starts a new coroutine that starts in parallel
    coroutineScope {
        launch { bathTime() } // this coroutine is a child of the coroutineScope
        launch { boilingWatter() }
    }
}

suspend fun noStructConcurrencyMorningRoutine() {
    // if you don't want a structure or a hierarchy then use GlobalScope
    GlobalScope.launch { bathTime() }
    GlobalScope.launch { boilingWatter() }
}
suspend fun main(array: Array<String>) {
//bathTime()
//sequentialMorningRoutine()
// concurrentMorningRoutine()
    noStructConcurrencyMorningRoutine()
    delay(2000) // needed for noStructConcurrencyMorningRoutine
}