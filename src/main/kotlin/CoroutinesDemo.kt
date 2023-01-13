import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlinx.coroutines.*
import kotlin.math.log

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

suspend fun makeCoffee() {
    logger.info("starting to make coffee")
    delay(500)
    logger.info("coffee is ready")
}

suspend fun morningRoutineWithCoffee() {
    coroutineScope {
        // job gives you control flow, launch 2 tasks in parallel and then wiat for both of them to complete before starting the 3rd task in parallel
        val bathTimeJob: Job = launch { bathTime() }
        val boilingWaterJob: Job = launch { boilingWatter() }
        bathTimeJob.join()
        boilingWaterJob.join()
        launch { makeCoffee() }
    }
}

// same as morningRoutineWithCoffee but more clean
suspend fun morningRoutineWithCoffeeStructured() {
    coroutineScope {
        coroutineScope {
            launch { bathTime() }
            launch { boilingWatter() }
        }
        launch { makeCoffee() }
    }
}

// return values from coroutines
suspend fun preparingJavaCoffee(): String {
    logger.info("starting to make java coffee")
    delay(500)
    logger.info("java coffee is ready")
    return "Java coffee"
}

suspend fun toastingBread(): String {
    logger.info("starting to make breakfast")
    delay(1000)
    logger.info("toast is ready")
    return "Toasted bread"
}

suspend fun prepareBreakfast() {
    coroutineScope {
        val coffee = async { preparingJavaCoffee() } // Deferred = analogous of Future[T] in scala
        val toast = async { toastingBread() }
        // semantic blocking
        val finalCoffee = coffee.await()
        val finalToast = toast.await()
        logger.info("I am eating $finalToast and drinking $finalCoffee")
    }
}

suspend fun main(array: Array<String>) {
//bathTime()
//sequentialMorningRoutine()
// concurrentMorningRoutine()
    //noStructConcurrencyMorningRoutine()
    //delay(2000) // needed for noStructConcurrencyMorningRoutine

    prepareBreakfast()
}