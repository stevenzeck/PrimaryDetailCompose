package com.example.primarydetailcompose.di

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.primarydetailcompose.util.BASE_URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class NetworkModuleTest {

    @Test
    fun `verify base url`() {
        val okHttpClient = NetworkModule.provideOkHttpClient()
        val retrofit = NetworkModule.provideRetrofit(okHttpClient = okHttpClient)

        val expected = if (BASE_URL.endsWith("/")) BASE_URL else "$BASE_URL/"
        assertEquals(expected, retrofit.baseUrl().toString())
    }

    @Test
    fun `verify api service provider`() {
        val okHttpClient = NetworkModule.provideOkHttpClient()
        val retrofit = NetworkModule.provideRetrofit(okHttpClient = okHttpClient)
        val apiService = NetworkModule.provideApiService(retrofit = retrofit)

        assertNotNull("ApiService should not be null", apiService)
    }
}
