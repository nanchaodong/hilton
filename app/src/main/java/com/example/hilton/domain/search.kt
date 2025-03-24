package com.example.hilton.domain

import com.example.PokemonQuery

interface SearchUseCase {
    suspend fun request(query: String): List<PokemonDomain>
}

private interface SearchRepository {
    suspend fun request(query: String): List<PokemonDomain>
}

private interface SearchService {
    suspend fun request(query: String): PokemonQuery.Data
}

data class PokemonDomain(
    val name: String,
    val captureRate: String,
    val color: String,
    val abilities: List<String>
)


private class SearchServiceImpl(private val netWorkUseCase: NetWorkUseCase) : SearchService {
    override suspend fun request(query: String): PokemonQuery.Data {
        return netWorkUseCase.query(PokemonQuery(query))
    }
}

private class SearchRepositoryImpl(private val service: SearchService) : SearchRepository {
    override suspend fun request(query: String): List<PokemonDomain> {
        return service.request(query).pokemon_v2_pokemonspecies.map {
            PokemonDomain(
                name = it.name,
                captureRate = "${it.capture_rate}%",
                color = it.pokemon_v2_pokemoncolor?.name.orEmpty(),
                abilities = it.pokemon_v2_pokemons.firstOrNull()?.pokemon_v2_pokemonabilities.orEmpty()
                    .map {
                        it.pokemon_v2_ability?.name.orEmpty()
                    }
            )
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
                createNetWorkUseCase()
            )
        )
    )
}

fun createSearchUseCase(searchUseCase: SearchUseCase = defaultSearchUseCase) = searchUseCase



