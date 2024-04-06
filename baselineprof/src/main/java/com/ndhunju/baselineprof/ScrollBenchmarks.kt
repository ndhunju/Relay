package com.ndhunju.baselineprof

import android.Manifest
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ScrollBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @get:Rule
    // These aren't granting permission to the app, neither in device or emulator
    // Currently, doing it manually
    val grantSmsPermission: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    @Test
    fun scrollCompilationNone() = scroll(CompilationMode.None())

    @Test
    fun scrollCompilationBaselineProfiles() = scroll(CompilationMode.Partial())

    private fun scroll(compilationMode: CompilationMode) {
        rule.measureRepeated(
            packageName = getTargetAppPackageName(),
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            startupMode = StartupMode.WARM,
            iterations = 10,
            setupBlock = {
                pressHome()
                startActivityAndWait(getLaunchIntentFor(".ui.MainActivity"))
                grantPermissionIfNeeded()
            },
            measureBlock = {
                scrollThreadListUpAndDown()
            }
        )
    }
}
