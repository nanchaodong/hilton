package com.example.hilton.domain

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Adapter
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.CustomScalarAdapters
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.api.json.JsonWriter
import com.apollographql.apollo.testing.QueueTestNetworkTransport
import com.apollographql.apollo.testing.enqueueTestResponse
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.assertThrows

data class TestQuery(val msg: String) : Query<TestQuery.Data> {
    data class Data(val msg: String) : Query.Data

    override fun adapter(): Adapter<Data> {
        TODO("Not yet implemented")
    }

    override fun document(): String {
        TODO("Not yet implemented")
    }

    override fun id(): String {
        TODO("Not yet implemented")
    }

    override fun name(): String {
        TODO("Not yet implemented")
    }

    override fun rootField(): CompiledField {
        TODO("Not yet implemented")
    }

    override fun serializeVariables(
        writer: JsonWriter,
        customScalarAdapters: CustomScalarAdapters,
        withDefaultValues: Boolean
    ) {
        TODO("Not yet implemented")
    }
}

@OptIn(ApolloExperimental::class)
class NetworkKtTest {
    @Test
    fun `test query with response`() = runTest {
        val useCase = object : ApolloUseCase {
            override val tool: ApolloClient = ApolloClient.Builder()
                .networkTransport(QueueTestNetworkTransport())
                .build()
        }
        val query = TestQuery("ivy")
        val data = TestQuery.Data(
            "name"
        )
        useCase.tool.enqueueTestResponse(query, data)
        val result = useCase.query(query, "")
        assert(result == data)

    }

    @Test
    fun `test query with empty response`() = runTest {
        val useCase = object : ApolloUseCase {
            override val tool: ApolloClient = ApolloClient.Builder()
                .networkTransport(QueueTestNetworkTransport())
                .build()
        }
        val query = TestQuery("ivy")
        useCase.tool.enqueueTestResponse(query, null)
        assertThrows<Exception> {
            useCase.query(query, "")
        }
    }

    @Test
    fun `test query with error`() = runTest {
        val useCase = object : ApolloUseCase {
            override val tool: ApolloClient = ApolloClient.Builder()
                .networkTransport(QueueTestNetworkTransport())
                .build()
        }
        val query = TestQuery("ivy")
        useCase.tool.enqueueTestResponse(
            query,
            null,
            listOf(Error.Builder(message = "error").build())
        )
        assertThrows<Exception> {
            useCase.query(query, "")
        }
    }
}