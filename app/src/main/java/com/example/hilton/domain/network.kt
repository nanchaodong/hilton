@file:Suppress("UNCHECKED_CAST")

package com.example.hilton.domain

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain

interface ApolloUseCase : StrongType {
    val tool: ApolloClient
}

suspend inline fun <reified T : Query.Data> ApolloUseCase.query(
    t: Query<T>,
    url: String
): T {
    val response = tool.query(t)
        .httpHeaders(listOf(HttpHeader(ENDPOINT, url)))
        .execute()
    return if (response.hasErrors()) {
        error("error")
    } else {
        response.data ?: error("error")
    }
}

class ApolloUseCaseImpl : ApolloUseCase {
    override val tool: ApolloClient by lazy {
        ApolloClient.Builder()
            .serverUrl("https://www.placeholder.com")
            .addHttpInterceptor(object : HttpInterceptor {
                // only use one apollo client for all request through this interceptor
                override suspend fun intercept(
                    request: HttpRequest,
                    chain: HttpInterceptorChain
                ): HttpResponse {
                    val headers = request.headers.toMutableList()
                    val header = headers.first { it.name == ENDPOINT }
                    headers.removeIf { it.name == ENDPOINT }
                    val newRequest = request.newBuilder(url = header.value)
                        .headers(headers)
                        .build()
                    return chain.proceed(newRequest)
                }

            })
            .build()
    }
}

const val ENDPOINT = "api-url"


