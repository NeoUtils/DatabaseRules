package com.neo.fbrules.util

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.neo.fbrules.core.Environment

val firebaseEnvironment: DatabaseReference by lazy {
    Firebase.database.getReference(Environment.FIREBASE)
}