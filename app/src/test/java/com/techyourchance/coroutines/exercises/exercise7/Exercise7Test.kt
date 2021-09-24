package com.techyourchance.coroutines.exercises.exercise7

import com.techyourchance.coroutines.common.TestUtils
import com.techyourchance.coroutines.common.TestUtils.printCoroutineScopeInfo
import com.techyourchance.coroutines.common.TestUtils.printJobsHierarchy
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import java.lang.Exception
import kotlin.coroutines.EmptyCoroutineContext

class Exercise7Test {

    /*
    Write nested withContext blocks, explore the resulting Job's hierarchy, test cancellation
    of the outer scope
     */
    @Test
    fun nestedWithContext() {
        runBlocking {
            val scopeJob = Job()
            val scope = CoroutineScope(scopeJob + CoroutineName("outer scope") + Dispatchers.IO)
            scope.launch {
                println("start 1st withContext")
//                this.printCoroutineScopeInfo()
                withContext(CoroutineName("1st withContext")) {
                    delay(100)
//                    this.printCoroutineScopeInfo()
                    try {
                        println("start 2nd withContext")
                        withContext(CoroutineName("2nd withContext")) {
                            printJobsHierarchy(scopeJob)
//                            this.printCoroutineScopeInfo()
                            try {
                                delay(100)
                                println("2nd withContext done")
                            } catch (e: CancellationException) {
                                println("2nd withContext cancelled")
                            }
                        }
                        println("1st withContext done")
                    } catch (e: CancellationException) {
                        println("1st withContext cancelled")
                    }
                }
            }
            scope.launch(CoroutineName("2nd outer scope")) {
                // cancel parent + child jobs at 150 ms mark, well within 1st and 2nd coroutines
                delay(150)
                println("cancel outer scope")
                scope.cancel()
            }
            scopeJob.join()
            println("test done")
        }
    }

    /*
    Launch new coroutine inside another coroutine, explore the resulting Job's hierarchy, test cancellation
    of the outer scope, explore structured concurrency
     */
    @Test
    fun nestedLaunchBuilders() {
        runBlocking {
            val scopeJob = Job()
            val scope = CoroutineScope(scopeJob + CoroutineName("outer scope") + Dispatchers.IO)
            // the difference with withContext is that it only changes the coroutinecontext
            // launching coroutines means that they will execute concurrently (with one another)
            scope.launch {
                println("launch nested coroutine")
//                this.printCoroutineScopeInfo()
                try {
                    delay(100)
                    launch(CoroutineName("nested coroutine")) {
//                        this.printCoroutineScopeInfo()
                        printJobsHierarchy(scopeJob)
                        try {
                            delay(100)
                            println("nested coroutine done")
                        } catch (e: CancellationException) {
                            println("nested coroutine cancelled")
                        }
//                    printJobsHierarchy(scopeJob) // 2nd outer scope won't be printed as it's been cancelled at this point
                    }
                    println("outer scope done")
                } catch (e: CancellationException) {
                    println("outer scope cancelled")
                }
            }
            scope.launch(CoroutineName("2nd outer scope")) {
                delay(150)
                println("cancel outer scope")
                scope.cancel()
            }
            scopeJob.join()
            println("test done")
        }
    }

    /*
    Launch new coroutine on "outer scope" inside another coroutine, explore the resulting Job's hierarchy,
    test cancellation of the outer scope, explore structured concurrency
     */
    @Test
    fun nestedCoroutineInOuterScope() {
        runBlocking {
            val scopeJob = Job()
            val scope = CoroutineScope(scopeJob + CoroutineName("outer scope") + Dispatchers.IO)
            scope.launch {
                delay(100)
                try {
                    // only this coroutine will be cancelled when cancel is called
                    scope.launch(CoroutineName("coroutine using outer scope")) {
                        printJobsHierarchy(scopeJob)
                        try {
                            delay(100)
                            println("coroutine using outer scope done")
                        } catch (e: CancellationException) {
                            println("coroutine using outer scope cancelled")
                        }
                    }
                    println("outer scope done")
                } catch (e: CancellationException) {
                    println("outer scope cancelled")
                }
            }

            scope.launch(CoroutineName("2nd outer scope")) {
                delay(150)
                println("cancel outer scope")
                scope.cancel()
            }

            scopeJob.join()
            println("test done")
        }
    }


}
