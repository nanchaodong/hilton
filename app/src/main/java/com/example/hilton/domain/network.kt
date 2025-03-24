@file:Suppress("UNCHECKED_CAST")

package com.example.hilton.domain

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query

interface NetWorkUseCase<T> {
    val tool: T
}

private class NetWorkUseCaseImpl(
    override val tool: ApolloClient
) : NetWorkUseCase<ApolloClient>

suspend inline fun <reified T : Query.Data> NetWorkUseCase<ApolloClient>.query(t: Query<T>): T {
    val response = tool.query(t).execute()
    return if (response.hasErrors()) {
        error("error")
    } else {
        response.data ?: error("error")
    }
}

val defaultWorkUseCase: NetWorkUseCase<ApolloClient> by lazy {
    NetWorkUseCaseImpl(
        tool = ApolloClient.Builder().serverUrl("https://beta.pokeapi.co/graphql/v1beta")
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

