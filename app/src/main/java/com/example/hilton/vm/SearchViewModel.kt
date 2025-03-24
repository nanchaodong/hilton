package com.example.hilton.vm

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hilton.domain.PokemonDomain
import com.example.hilton.domain.createSearchUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed interface Action {
    data class QueryChange(val query: String) : Action
}

@Immutable
data class PokemonUi(
    val name: String,
    val captureRate: String,
    val color: String,
    @Stable
    val abilities: List<String>
)

private fun PokemonDomain.convertor(): PokemonUi {
    return PokemonUi(
        name = name,
        captureRate = captureRate,
        color = color,
        abilities = abilities
    )
}

private fun List<PokemonDomain>.convertor(): List<PokemonUi> {
    return map {
        it.convertor()
    }
}

sealed interface SearchResultState {
    data object Init : SearchResultState
    data object Loading : SearchResultState
    data class Success(val pokemons: List<PokemonUi>) : SearchResultState
    data object Error : SearchResultState
}

data class QueryData(val query: String, val time: Long = System.currentTimeMillis())

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel : ViewModel() {
    private val _query = MutableStateFlow(QueryData(""))
    val query: Flow<String>
        get() = _query.map { it.query }.distinctUntilChanged()
    private val _result = MutableStateFlow<SearchResultState>(SearchResultState.Init)
    val result: Flow<SearchResultState>
        get() = _result
    private val useCase by lazy {
        createSearchUseCase()
    }

    init {
        viewModelScope.launch {
            _query.debounce(200)
                .mapLatest {
                    if (it.query.isEmpty()) {
                        SearchResultState.Init
                    } else {
                        _result.value = SearchResultState.Loading
                        kotlin.runCatching {
                            useCase.request(it.query)
                        }.fold(onSuccess = {
                            SearchResultState.Success(it.convertor())
                        }, onFailure = {
                            SearchResultState.Error
                        })
                    }
                }
                .onStart {
                    emit(SearchResultState.Init)
                }
                .collectLatest {
                    _result.value = it
                }
        }
    }

    fun sendAction(action: Action) {
        when (action) {
            is Action.QueryChange -> {
                _query.value = QueryData(action.query)
            }
        }
    }

}