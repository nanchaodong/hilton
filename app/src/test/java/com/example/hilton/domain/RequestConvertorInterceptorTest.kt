package com.example.hilton.domain

import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.api.http.HttpMethod
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.network.http.HttpInterceptorChain
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

class RequestConvertorInterceptorTest {
    @Test
    fun `test interceptor`() = runTest {
        val interceptor = RequestConvertorInterceptor()
        val request = HttpRequest.Builder(method = HttpMethod.Get, url = "https://www.google.com")
            .headers(listOf(HttpHeader(ENDPOINT, "a")))
            .build()
        val chain = object : HttpInterceptorChain {
            override suspend fun proceed(request: HttpRequest): HttpResponse {
                return HttpResponse.Builder(statusCode = 200).build()
            }

        }
        val response = interceptor.intercept(request, chain)
        assertTrue {
            response.statusCode == 200
        }
    }
}