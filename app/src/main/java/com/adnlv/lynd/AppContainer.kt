package com.adnlv.lynd

import android.content.Context
import androidx.room.Room
import com.adnlv.lynd.data.db.AppDatabase
import com.adnlv.lynd.data.network.NbuApiService
import com.adnlv.lynd.data.network.NbuRepository
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "lynd_database"
    ).fallbackToDestructiveMigration().build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://bank.gov.ua/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val nbuApiService: NbuApiService = retrofit.create(NbuApiService::class.java)

    val nbuRepository: NbuRepository = NbuRepository(
        apiService = nbuApiService,
        bondDao = database.bondDao(),
        syncMetadataDao = database.syncMetadataDao()
    )
}
