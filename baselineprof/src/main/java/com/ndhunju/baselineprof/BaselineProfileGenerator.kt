package com.ndhunju.baselineprof

import android.Manifest
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test class generates a basic startup baseline profile for the target package.
 *
 * We recommend you start with this but add important user flows to the profile to improve their performance.
 * Refer to the [baseline profile documentation](https://d.android.com/topic/performance/baselineprofiles)
 * for more information.
 *
 * You can run the generator with the "Generate Baseline Profile" run configuration in Android Studio or
 * the equivalent `generateBaselineProfile` gradle task:
 * ```
 * ./gradlew :app:generateReleaseBaselineProfile
 * ```
 * The run configuration runs the Gradle task and applies filtering to run only the generators.
 *
 * Check [documentation](https://d.android.com/topic/performance/benchmarking/macrobenchmark-instrumentation-args)
 * for more information about available instrumentation arguments.
 *
 * After you run the generator, you can verify the improvements running the [StartupBenchmarks] benchmark.
 *
 * When using this class to generate a baseline profile, only API 33+ or rooted API 28+ are supported.
 *
 * The minimum required version of androidx.benchmark to generate a baseline profile is 1.2.0.
 **/
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule // These aren't granting permission to the app, neither in device or emulator
    val grantSmsPermission: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS
    )

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        // The application id for the running build variant is read from the instrumentation arguments.
        rule.collect(
            packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
                ?: throw Exception("targetAppId not passed as instrumentation runner arg"),

            // See: https://d.android.com/topic/performance/baselineprofiles/dex-layout-optimizations
            includeInStartupProfile = true
        ) {
            // This block defines the app's critical user journey. Here we are interested in
            // optimizing for app startup. But you can also navigate and scroll through your most important UI.

            // Start default activity for your app
            pressHome()
            startActivityAndWait()

            createAccountIfNeeded()
            grantPermissionIfNeeded()
            scrollThreadListUpAndDown()

            // Check UiAutomator documentation for more information how to interact with the app.
            // https://d.android.com/training/testing/other-components/ui-automator

        }

    }

    private fun createAccountIfNeeded() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val createAccountBtn = device.findObject(By.text("Create Account"))

        if (createAccountBtn != null) {
            createAccountBtn.click()
            device.waitForIdle()

            // Enter phone number
            val phoneTextField = device.findObject(By.res("Phone"))
            phoneTextField.text = "2546709494"

            // Click Create
            device.findObject(By.text("Create")).click()
            device.waitForWindowUpdate(null, 3_000L)
        }
    }

    private fun MacrobenchmarkScope.grantPermissionIfNeeded() {
        val grantSmsPermissionBtn = device.findObject(By.text("Grant Permission"))
        if (grantSmsPermissionBtn != null) {
            // These aren't granting permission to the app, neither in device or emulator
            val cmd = "pm grant " + device.currentPackageName
            device.executeShellCommand(cmd+ Manifest.permission.RECEIVE_SMS)
            device.executeShellCommand(cmd + Manifest.permission.READ_SMS)
            device.executeShellCommand(cmd + Manifest.permission.SEND_SMS)
            //grantSmsPermissionBtn.click()
        }
    }

    private fun MacrobenchmarkScope.scrollThreadListUpAndDown() {
        // Wait until content is asynchronously loaded.
        device.wait(Until.hasObject(By.res("threadList")), 3_000)
        // We find element with resource-id
        val threadList = device.findObject(By.res("threadList")) ?: return

        // Set some margin to prevent triggering system navigation
        threadList.setGestureMargin(device.displayWidth / 5)

        // Scroll up and down
        threadList.fling(Direction.DOWN)
        device.waitForIdle()
        threadList.fling(Direction.UP)
    }

}