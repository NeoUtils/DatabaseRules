package com.neo.fbrules

import android.app.Application
import com.neo.fbrules.util.environment
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        environment = getString(R.string.environment)
        super.onCreate()
    }
}