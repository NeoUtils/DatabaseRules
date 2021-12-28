package com.neo.fbrules.main.domain.model

import com.neo.fbrules.main.data.model.DataCredential

fun DomainCredential.toData(): DataCredential {
    return DataCredential(
        privateKey = privateKey,
        databaseKey = databaseKey
    )
}