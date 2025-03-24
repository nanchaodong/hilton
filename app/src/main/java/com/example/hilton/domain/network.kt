package com.example.hilton.domain

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query

interface NetWorkUseCase {
    val apolloClient: ApolloClient
}

private class NetWorkUseCaseImpl(override val apolloClient: ApolloClient) : NetWorkUseCase

private val defaultWorkUseCase: NetWorkUseCase by lazy {
    NetWorkUseCaseImpl(
        apolloClient = ApolloClient.Builder().serverUrl("https://beta.pokeapi.co/graphql/v1beta")
            .build()
    )
}

fun createNetWorkUseCase(useCase: NetWorkUseCase = defaultWorkUseCase) = useCase

suspend inline fun <reified T : Query.Data> NetWorkUseCase.query(t: Query<T>): T {
    val response = apolloClient.query(t).execute()
    return if (response.hasErrors()) {
        error("error")
    } else {
        response.data ?: error("error")
    }
}
