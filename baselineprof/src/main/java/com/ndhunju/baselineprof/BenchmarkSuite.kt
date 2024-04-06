package com.ndhunju.baselineprof

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite::class)
@SuiteClasses(StartupBenchmarks::class, ScrollBenchmarks::class)
class BenchmarkSuite