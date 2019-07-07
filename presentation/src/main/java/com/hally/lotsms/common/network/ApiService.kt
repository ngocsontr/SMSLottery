package com.hally.lotsms.common.network

import com.hally.lotsms.BuildConfig
import com.hally.lotsms.common.network.model.XsmbRss
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by HallyTran on 4/6/2019.
 * transon97uet@gmail.com
 */
internal interface ApiService {
    companion object {
        fun create(baseUrl: String): ApiService {

            val client = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
//                                .addHeader("Connection", "keep-alive")
//                                .addHeader("cache-control", "no-cache")
                                .build()
                        chain.proceed(request)
                    }
                    .addInterceptor(HttpLoggingInterceptor().setLevel(
                            if (BuildConfig.DEBUG)
                                HttpLoggingInterceptor.Level.BODY
                            else
                                HttpLoggingInterceptor.Level.NONE)).build()

            val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            return retrofit.create(ApiService::class.java)
        }
    }

    @GET("v1/api.json")
    fun getRss(@Query("rss_url") rss_url: String): Call<XsmbRss>

}
