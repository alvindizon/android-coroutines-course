package com.techyourchance.coroutines.exercises.exercise6

import com.techyourchance.coroutines.common.ThreadInfoLogger.logThreadInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class Exercise6BenchmarkUseCase(private val postBenchmarkResultsEndpoint: PostBenchmarkResultsEndpoint) {

    suspend fun executeBenchmark(benchmarkDurationSeconds: Int) = withContext(Dispatchers.IO) {
        logThreadInfo("benchmark started")

        val stopTimeNano = System.nanoTime() + benchmarkDurationSeconds * 1_000_000_000L

        var iterationsCount: Long = 0
        while (System.nanoTime() < stopTimeNano) {
            ensureActive()
            iterationsCount++
        }

        logThreadInfo("benchmark completed")

        postBenchmarkResultsEndpoint.postBenchmarkResults(benchmarkDurationSeconds, iterationsCount)

        logThreadInfo("benchmark results posted to the server")

        iterationsCount
    }

}
