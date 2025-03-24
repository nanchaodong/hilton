package com.example.hilton.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.hilton.vm.PokemonUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Detail(pokemonUi: PokemonUi, back: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text(pokemonUi.name)
        }, navigationIcon = {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
                modifier = Modifier.clickable(onClick = back)
            )
        })
        Text("name: pokemonUi.name")
        if (pokemonUi.abilities.isNotEmpty()) {
            Text("abilities:")
            pokemonUi.abilities.forEach {
                Text(it)
            }
        }
    }
}