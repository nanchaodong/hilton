package com.example.hilton.domain

import com.apollographql.apollo.network.http.HttpNetworkTransport
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

class ApolloUseCaseImplTest {
    @Test
    fun `test apollo client`() {
        val impl = ApolloUseCaseImpl()
        assertTrue {
            impl.tool.networkTransport is HttpNetworkTransport
        }
    }
}