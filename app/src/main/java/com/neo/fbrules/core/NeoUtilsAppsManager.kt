package com.neo.fbrules.core

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.main.presenter.model.NeoUtilsApp
import com.neo.fbrules.util.firebaseEnvironment


class NeoUtilsAppsManager(
    private val appsListener: AppsListener
) : ValueEventListener {

    private val fbNeoUtilsAppsManager =
        firebaseEnvironment.child("neo_utils_apps")

    init {
        fbNeoUtilsAppsManager.addValueEventListener(this)
    }

    interface AppsListener {
        fun change(apps: List<NeoUtilsApp>)
    }

    override fun onDataChange(snapshot: DataSnapshot) {

        val neoUtilsApp = mutableListOf<NeoUtilsApp>()

        for (child in snapshot.children) {

            runCatching {
                val name = child.child("name").value as String
                val description = child.child("description-pt").value as String
                val packageName = child.child("package").value as String
                val url = child.child("url").value as String
                val iconUrl = child.child("icon").value as String

                neoUtilsApp.add(
                    NeoUtilsApp(
                        name = name,
                        description = description,
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
