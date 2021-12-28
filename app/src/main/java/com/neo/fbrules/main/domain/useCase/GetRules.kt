package com.neo.fbrules.main.domain.useCase

import com.neo.fbrules.core.Result
import com.neo.fbrules.main.data.FirebaseRepository
import com.neo.fbrules.main.domain.model.DomainCredential
import com.neo.fbrules.main.domain.model.toData
import javax.inject.Inject

interface GetRules {
    suspend operator fun invoke(
        domainCredential: DomainCredential
    ): Result<String>
}

class GetRulesImpl @Inject constructor(
    private val firebaseRepository: FirebaseRepository
) : GetRules {

    override suspend fun invoke(domainCredential: DomainCredential): Result<String> {
        return firebaseRepository.getRules(
            credential = domainCredential.toData()
        )
    }
}