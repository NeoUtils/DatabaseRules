package com.neo.fbrules

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.util.environment
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {

        FirebaseDatabase
            .getInstance()
            .setPersistenceEnabled(true)

        environment = getString(R.string.environment)

        super.onCreate()
    }
}