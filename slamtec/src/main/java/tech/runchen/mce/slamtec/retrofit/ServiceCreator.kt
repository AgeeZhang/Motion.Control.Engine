package tech.runchen.mce.slamtec.retrofit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tech.runchen.mce.slamtec.BuildConfig
import tech.runchen.mce.slamtec.retrofit.model.api.ChassisApi
import tech.runchen.mce.slamtec.retrofit.model.api.CloudApi
import java.net.Proxy
import java.util.concurrent.TimeUnit

object ServiceCreator {

    private fun provideOkHttpClient() = run {
        val logging = HttpLoggingInterceptor()
        logging.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .proxy(Proxy.NO_PROXY)
            .build()
    }

    fun createChassisService(baseUrl: String): ChassisApi =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(provideOkHttpClient())
            .build().create(ChassisApi::class.java)

    fun createCloudService(baseUrl: String): CloudApi =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .client(provideOkHttpClient())
            .build().create(CloudApi::class.java)

}