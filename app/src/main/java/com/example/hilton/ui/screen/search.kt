package com.example.hilton.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hilton.ui.Empty
import com.example.hilton.ui.Error
import com.example.hilton.ui.Loading
import com.example.hilton.vm.Action
import com.example.hilton.vm.PokemonUi
import com.example.hilton.vm.SearchResultState
import com.example.hilton.vm.SearchViewModel

@Composable
fun SearchScreen(toDetail: (PokemonUi) -> Unit) {
    val vm = viewModel<SearchViewModel>()
    Column(modifier = Modifier.fillMaxSize()) {
        val query by vm.query.collectAsStateWithLifecycle("")
        SearchHeader(query) {
            vm.sendAction(Action.QueryChange(it))
        }
        val searchResult by vm.result.collectAsStateWithLifecycle(SearchResultState.Init)
        SearchResult(searchResult, retry = {
            vm.sendAction(Action.QueryChange(query))
        }, toDetail)
    }
}

@Composable
private fun SearchHeader(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    )
}

@Composable
private fun SearchResult(
    state: SearchResultState,
    retry: () -> Unit,
    toDetail: (PokemonUi) -> Unit
) {
    Crossfade(state) {
        when (it) {
            SearchResultState.Error -> {
                Error(retry)
            }

            SearchResultState.Loading -> {
                Loading()
            }

            is SearchResultState.Success -> {
                Success(it.pokemons, toDetail)
            }

            SearchResultState.Init -> Unit
        }
    }
}

@Composable
fun Success(pokemons: List<PokemonUi>, toDetail: (PokemonUi) -> Unit) {
    if (pokemons.isEmpty()) {
        Empty()
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(pokemons) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = {
                            toDetail.invoke(it)
                        })
                        .background(color = color(it.color))
                ) {
                    Text("name: ${it.name}")
                    Text("capture rate: ${it.captureRate}")
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun color(color: String): Color {
    return when (color) {
        "black" -> Color.Black
        "blue" -> Color.Blue
        "brown" -> Color(101, 66, 34)
        "gray" -> Color.Gray
        "green" -> Color.Green
        "pink" -> Color(241, 157, 173)
        "purple" -> Color(83, 8, 118)
        "red" -> Color.Red
        "yellow" -> Color.Yellow
        else -> Color.White
    }
}
