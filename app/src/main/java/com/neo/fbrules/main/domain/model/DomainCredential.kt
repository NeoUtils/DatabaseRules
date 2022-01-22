package com.neo.fbrules.main.domain.model

import com.neo.fbrules.main.data.model.DataCredential

data class DomainCredential(
    var privateKey: String,
    var databaseKey: String
)

fun DomainCredential.toData(): DataCredential {
    return DataCredential(
        privateKey = privateKey,
        databaseKey = databaseKey
    )
}