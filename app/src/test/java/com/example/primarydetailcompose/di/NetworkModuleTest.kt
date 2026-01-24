package com.example.primarydetailcompose.di

import com.example.primarydetailcompose.util.BASE_URL
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkModuleTest {

    @Test
    fun `verify base url`() {
        val okHttpClient = NetworkModule.provideOkHttpClient()
        val retrofit = NetworkModule.provideRetrofit(okHttpClient = okHttpClient)

        val expected = if (BASE_URL.endsWith("/")) BASE_URL else "$BASE_URL/"
        assertEquals(expected, retrofit.baseUrl().toString())
    }
}
