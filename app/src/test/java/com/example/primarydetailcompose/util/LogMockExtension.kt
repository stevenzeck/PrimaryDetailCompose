package com.example.primarydetailcompose.util

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class LogMockExtension : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext?) {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    override fun afterEach(context: ExtensionContext?) {
        unmockkStatic(Log::class)
    }
}
