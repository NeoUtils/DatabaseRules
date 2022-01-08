package com.neo.fbrules.main.data

import com.neo.fbrules.core.Result
import com.neo.fbrules.main.data.api.FirebaseApi
import com.neo.fbrules.main.data.model.DataCredential
import com.neo.fbrules.util.errorMessage
import com.neo.fbrules.util.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject

interface FirebaseRepository {

    suspend fun getRules(credential: DataCredential): Result<String>
    suspend fun setRules(rules: String, credential: DataCredential): Result<Unit>
}

/**
 * @author Irineu A. Silva
 */
class FirebaseRepositoryImpl @Inject constructor(
    private val service: FirebaseApi
) : FirebaseRepository {

    override suspend fun getRules(credential: DataCredential): Result<String> {

        return runCatching {
            val result = service.getRules(
                credential.databaseKey,
                credential.privateKey
            )

            Result.Success(result)
        }.getOrElse(::onFailure)
    }

    override suspend fun setRules(
        rules: String,
        credential: DataCredential
    ): Result<Unit> {

        return runCatching {
            val requestBody = rules.toRequestBody()

            service.setRules(
                requestBody,
                credential.databaseKey,
                credential.privateKey
            )

            Result.Success(Unit)
        }.getOrElse(::onFailure)
    }

    private fun onFailure(it: Throwable) = if (it is HttpException) {
        Result.Error(
            title = "${it.message}\n",
            message = "${it.response()?.errorBody()?.errorMessage()}"
        )
    } else {
        Result.Error(
            title = "${it::class.simpleName}\n",
            message = "${it.message}"
        )
    }
}