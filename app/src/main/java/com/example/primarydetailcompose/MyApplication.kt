package com.example.primarydetailcompose

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base Application class for the app.
 *
 * This class triggers Hilt code generation and adds an application-level dependency container.
 */
@HiltAndroidApp
class MyApplication : Application()
