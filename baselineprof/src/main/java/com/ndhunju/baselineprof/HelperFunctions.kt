package com.ndhunju.baselineprof

import android.Manifest
import android.content.Intent
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until


fun getTargetAppPackageName(): String {
    return InstrumentationRegistry.getArguments().getString("targetAppId")
        ?: throw Exception("targetAppId not passed as instrumentation runner arg")
}

fun getLoginActivityLaunchIntent(): Intent {
    return getLaunchIntentFor(".ui.login.LoginActivity")
}

/**
 * @param activityName Pass the path as define in AndroidManifest.xml file for that activity
 */
fun getLaunchIntentFor(activityName: String): Intent {
    return Intent().apply {
        setClassName(getTargetAppPackageName(), "com.ndhunju.relay$activityName")
    }
}

fun createAccountIfNeeded() {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val createAccountBtn = device.findObject(By.text("Create Account"))

    if (createAccountBtn != null) {
        createAccountBtn.click()
        device.waitForIdle(5_000)

        // Enter phone number
        val phoneTextField = device.findObject(By.res("Phone"))
        phoneTextField.text = "2546709494"

        // Click Create
        device.findObject(By.text("Create")).click()
        device.waitForWindowUpdate(null, 5_000L)
    }
}

fun MacrobenchmarkScope.grantPermissionIfNeeded() {
    val grantSmsPermissionBtn = device.findObject(By.text("Grant Permission"))
    if (grantSmsPermissionBtn != null) {
        // These aren't granting permission to the app, neither in device or emulator
        // Currently, doing it manually
        val cmd = "pm grant " + device.currentPackageName
        device.executeShellCommand(cmd+ Manifest.permission.RECEIVE_SMS)
        device.executeShellCommand(cmd + Manifest.permission.READ_SMS)
        device.executeShellCommand(cmd + Manifest.permission.SEND_SMS)
        //grantSmsPermissionBtn.click()
    }
}

fun MacrobenchmarkScope.scrollThreadListUpAndDown() {
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