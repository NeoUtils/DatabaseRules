package com.neo.fbrules.core.constants

 fun setupConditions(getIsPortuguese : () -> Boolean) = lazy {
    if (getIsPortuguese()) {
        arrayListOf(
            "Nenhum" to "false",
            "Apenas o usuário" to "auth.uid == \$uid",
            "Apenas autenticado" to "auth != null",
            "Todos" to "true"
        )
    } else {
        arrayListOf(
            "None" to "false",
            "Only user" to "auth.uid == \$uid",
            "Just authenticated" to "auth != null",
            "All" to "true"
        )

    }
}

 fun setupProperties(getIsPortuguese : () -> Boolean) = lazy {
    if (getIsPortuguese()) {
        arrayListOf(
            "Leitura" to ".read",
            "Escrita" to ".write"
        )
    } else {
        arrayListOf(
            "Read" to ".read",
            "Write" to ".write"
        )
    }
}
