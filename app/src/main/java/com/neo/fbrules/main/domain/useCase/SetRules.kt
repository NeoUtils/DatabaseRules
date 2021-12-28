package com.neo.fbrules.main.domain.useCase

import com.neo.fbrules.core.Result
import com.neo.fbrules.main.data.FirebaseRepository
import com.neo.fbrules.main.domain.model.DomainCredential
import com.neo.fbrules.main.domain.model.toData
import javax.inject.Inject

interface SetRules {
    suspend operator fun invoke(
        rules: String,
        credential: DomainCredential
    ): Result<Unit>
}

class SetRulesImpl @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : SetRules {
    override suspend fun invoke(
        rules: String,
        credential: DomainCredential
    ): Result<Unit> {
        return firebaseRepository.setRules(
            rules = rules,
            credential = credential.toData()
        )
    }

}