package com.neo.fbrules.util

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

lateinit var environment : String

val firebaseEnvironment: DatabaseReference by lazy {
    Firebase.database.getReference(environment)
}