package com.neo.fbrules.main.data.api

import com.neo.fbrules.core.Constants.FIREBASE_RULES
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface FirebaseApi {

    @GET("https://{db}.firebaseio.com/$FIREBASE_RULES")
    suspend fun getRules(
        @Path("db") db: String,
        @Query("auth") token: String
    ): String

    @PUT("https://{db}.firebaseio.com/$FIREBASE_RULES")
    suspend fun setRules(
        @Body rules: RequestBody,
        @Path("db") db: String,
        @Query("auth") token: String
    )

    companion object {
        val service: FirebaseApi by lazy {

            val retrofit = Retrofit.Builder()
                .baseUrl("https://localhost/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit.create(FirebaseApi::class.java)
        }
    }
}