package com.neo.fbrules.main.domain.model

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.main.presenter.model.IntegratedApp
import com.neo.fbrules.util.firebaseEnvironment


class IntegratedAppsManager(
    private val appsListener: AppsListener
) : ValueEventListener {

    private val fbNeoUtilsAppsManager =
        firebaseEnvironment.child("neo_utils_apps")

    init {
        fbNeoUtilsAppsManager.addValueEventListener(this)
    }

    interface AppsListener {
        fun change(apps: List<IntegratedApp>)
    }

    override fun onDataChange(snapshot: DataSnapshot) {

        val neoUtilsApp = mutableListOf<IntegratedApp>()

        for (child in snapshot.children) {

            runCatching {
                val name = child.child("name").value as String
                val packageName = child.child("package").value as String
                val url = child.child("url").value as String
                val iconUrl = child.child("icon").value as String

                neoUtilsApp.add(
                    IntegratedApp(
                        name = name,
                        packageName = packageName,
                        url = url,
                        iconUrl = iconUrl
                    )
                )
            }.onFailure {
                Firebase.crashlytics.recordException(it)
            }
        }

        appsListener.change(neoUtilsApp)

    }

    override fun onCancelled(error: DatabaseError) = Unit
}
