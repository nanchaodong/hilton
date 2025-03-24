package com.example.hilton.domain

import com.apollographql.apollo.ApolloClient
import com.example.PokemonQuery

interface SearchUseCase {
    suspend fun request(query: String): List<PokemonDomain>
}

private interface SearchRepository {
    suspend fun request(query: String): List<PokemonDomain>
}

private interface SearchService {
    suspend fun request(query: String): List<PokemonDomain>
}

data class PokemonDomain(
    val name: String, val captureRate: String, val color: String, val abilities: List<String>
)


private class SearchServiceImpl(private val netWorkUseCase: NetWorkUseCase<ApolloClient>) :
    SearchService {
    override suspend fun request(query: String): List<PokemonDomain> {
        return netWorkUseCase.query(PokemonQuery(query)).pokemon_v2_pokemonspecies.map {
            PokemonDomain(name = it.name,
                captureRate = "${it.capture_rate}%",
                color = it.pokemon_v2_pokemoncolor?.name.orEmpty(),
                abilities = it.pokemon_v2_pokemons.firstOrNull()?.pokemon_v2_pokemonabilities.orEmpty()
                    .map {
                        it.pokemon_v2_ability?.name.orEmpty()
                    })
        }
    }
}

private class SearchRepositoryImpl(private val service: SearchService) : SearchRepository {
    private val cache = mutableMapOf<String, List<PokemonDomain>>()
    override suspend fun request(query: String): List<PokemonDomain> {
        return cache[query] ?: run {
            val response = service.request(query)
            cache[query] = response
            response
        }

    }
}

private class SearchUseCaseImpl(private val repository: SearchRepository) : SearchUseCase {
    override suspend fun request(query: String): List<PokemonDomain> {
        return repository.request(query)
    }
}

private val defaultSearchUseCase: SearchUseCase by lazy {
    SearchUseCaseImpl(
        SearchRepositoryImpl(
            SearchServiceImpl(
                createNetWorkUseCase<ApolloClient>()
            )
        )
    )
}

fun createSearchUseCase(searchUseCase: SearchUseCase = defaultSearchUseCase) = searchUseCase



