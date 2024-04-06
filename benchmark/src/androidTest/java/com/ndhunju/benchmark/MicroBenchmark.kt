package com.ndhunju.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Arrays
import kotlin.random.Random

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will
 * output the result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class MicroBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    // Using random with the same seed, so that it generates the same data every run
    private val random = Random(0)

    // Create the array once and just copy it in benchmarks
    private val unsorted = IntArray(10_000) { random.nextInt() }

    private var listToSort = IntArray(unsorted.size)

    @Test
    fun microBenchmarkSortAlgo() {
        // Note that you cannot benchmark your code in an app module this way
        // You will need to move any code you want to benchmark to a library module:
        // TODO: Replace this with a code in the app module
        benchmarkRule.measureRepeated {
            // Copy the array with timing disabled to measure only the algorithm itself
            listToSort = runWithTimingDisabled { unsorted.copyOf() }

            // Sort the array in place and measure how long it takes
            Arrays.sort(listToSort)
        }

        // assert only once not to add overhead to the benchmarks
        assertTrue(listToSort.isSorted())

    }
}

fun IntArray.isSorted(): Boolean {
    for (i in this.indices) {
        if ((i+1) < size && this[i] > this[i+1]) {
            return false
        }
    }

    return true
}