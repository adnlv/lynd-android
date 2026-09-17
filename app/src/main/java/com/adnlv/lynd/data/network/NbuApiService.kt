package com.adnlv.lynd.data.network

import retrofit2.http.GET

interface NbuApiService {
    @GET("depo_securities?json")
    suspend fun getSecurities(): List<NbuSecurityDto>
}
