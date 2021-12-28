package com.neo.fbrules.core

object Constants {
    const val FIREBASE_RULES = ".settings/rules.json"

    enum class ERROR(val message: String) {
        CREDENTIAL_NOT_FOUND("Credenciais de acesso não configuradas"),
        UNKNOWN_ERROR("Erro não especificado"),
    }
}