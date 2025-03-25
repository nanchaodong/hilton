@file:Suppress("UNCHECKED_CAST")

package com.example.hilton.domain

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain

interface NetWorkUseCase<T> {
    val tool: T
}

private class NetWorkUseCaseImpl(
    override val tool: ApolloClient
) : NetWorkUseCase<ApolloClient>

const val ENDPOINT = "api-url"
suspend inline fun <reified T : Query.Data> NetWorkUseCase<ApolloClient>.query(
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

val defaultWorkUseCase: NetWorkUseCase<ApolloClient> by lazy {
    NetWorkUseCaseImpl(
        tool = ApolloClient.Builder()
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
    )
}

inline fun <reified T> createNetWorkUseCase(useCase: NetWorkUseCase<T>? = null): NetWorkUseCase<T> {
    return useCase
        ?: if (defaultWorkUseCase.tool is T) {
            defaultWorkUseCase as NetWorkUseCase<T>
        } else {
            error("please provide valid use case in parameter")
        }
}

