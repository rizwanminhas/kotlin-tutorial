import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlinx.coroutines.*
import java.util.concurrent.Executors
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

// 1- cooperative scheduling -  coroutines yield manually
suspend fun workingHard() {
    logger.info("Working")
    while (true) {

    }
    delay(100)
    logger.info("work done")
}

suspend fun workingNicely() {
    logger.info("Working")
    while (true) {
        delay(100) // give a chance to the dispatcher to run another coroutine
    }
    delay(100)
    logger.info("work done")
}


suspend fun takeABreak() {
    logger.info("Taking a break")
    delay(1000)
    logger.info("break done")
}

suspend fun workHardRoutine() {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)
    coroutineScope {
        launch(dispatcher) { workingHard() }
        launch(dispatcher) { takeABreak() }
    }
}

suspend fun workNicelyRoutine() {
    val dispatcher: CoroutineDispatcher = Dispatchers.Default.limitedParallelism(1)
    coroutineScope {
        launch(dispatcher) { workingNicely() }
        launch(dispatcher) { takeABreak() }
    }
}

val simpleDispatcher = Dispatchers.Default // "normal code" = short code or yielding coroutines.
val blockingDispatcher = Dispatchers.IO // blocking code = db connections, long running computations.
val customDispatcher = Executors.newFixedThreadPool(8).asCoroutineDispatcher()

// cancellation
suspend fun forgettingFriendBirthdayRoutine() {
    coroutineScope {
        val workingJob = launch { workingNicely() }
        launch {
            delay(2000) // after 2seconds I remember I have a birthday today
            workingJob.cancel() // sends a signal to the coroutine to cancel, cancellations happens at first yielding point
            workingJob.join() // you are sure that the coroutine has been cancelled
            logger.info("forgot friend's birthday, buying a present")
        }
    }
}

// if a coroutine doesn't yield, it can't be cancelled
suspend fun forgettingFriendBirthdayRoutineUncancellable() {
    coroutineScope {
        val workingJob = launch { workingHard() }
        launch {
            delay(2000) // after 2seconds I remember I have a birthday today
            logger.info("trying to stop working...")
            workingJob.cancel() // sends a signal to the coroutine to cancel, cancellations happens at first yielding point
            workingJob.join() // you are sure that the coroutine has been cancelled
            logger.info("forgot friend's birthday, buying a present")
        }
    }
}

// resources
class Desk : AutoCloseable {
    init {
        logger.info("starting to work on this desk")
    }

    override fun close() {
        logger.info("cleaning up the desk")
    }
}

suspend fun forgettingFriendBirthdayRoutineWithResource() {
    val desk = Desk()
    coroutineScope {
        val workingJob = launch {
            desk.use { _ -> // this resource will be closed upon the completion of the coroutine
                workingNicely()
            }
        }

        workingJob.invokeOnCompletion { exception: Throwable? ->
            // can handle completion and cancellation differently, depending on the exception
            logger.info("make sure I talk to my colleagues that i will be out for 30 mins")
        }
        launch {
            delay(2000) // after 2seconds I remember I have a birthday today
            workingJob.cancel() // sends a signal to the coroutine to cancel, cancellations happens at first yielding point
            workingJob.join() // you are sure that the coroutine has been cancelled
            logger.info("forgot friend's birthday, buying a present")
        }
    }
}

// canclellation propagates to child coroutines

suspend fun drinkWater() {
    while(true) {
        logger.info("drinking water")
        delay(1000)
    }
}

suspend fun forgettingFriendBirthdayRoutineStayHydrated() {
    coroutineScope {
        val workingJob = launch {
            launch { workingNicely() }
            launch { drinkWater() }
        }
        launch {
            delay(2000) // after 2seconds I remember I have a birthday today
            workingJob.cancel() // sends a signal to the coroutine to cancel, cancellations happens at first yielding point
            workingJob.join() // you are sure that the coroutine has been cancelled
            logger.info("forgot friend's birthday, buying a present")
        }
    }
}

// coroutines context
suspend fun asyncGreeting() {
    coroutineScope {
        launch(CoroutineName("Greeting Coroutine") + Dispatchers.Default /* these two = CoroutineContext */) {
            logger.info("hello, y'all!")
        }
    }
}

suspend fun demoContextInheritance() {
    coroutineScope {
        launch(CoroutineName("Greeting coroutine")) {
            logger.info("[parent coroutine] hello")
            launch {// coroutine context will be inherited here
                logger.info("[child coroutine] hi there")
            }
            delay(200)
            logger.info("[parent coroutine] hi again from parent")
        }
    }
}

suspend fun main(array: Array<String>) {
    //bathTime()
    //sequentialMorningRoutine()
    // concurrentMorningRoutine()

    //noStructConcurrencyMorningRoutine()
    //delay(2000) // needed for noStructConcurrencyMorningRoutine

    //prepareBreakfast()
    //workNicelyRoutine()
    //forgettingFriendBirthdayRoutine()
    //forgettingFriendBirthdayRoutineWithResource()
    //forgettingFriendBirthdayRoutineStayHydrated()
    //asyncGreeting()
    demoContextInheritance()
}