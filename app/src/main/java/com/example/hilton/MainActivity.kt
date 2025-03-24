package com.example.hilton

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hilton.ui.screen.Detail
import com.example.hilton.ui.screen.Route
import com.example.hilton.ui.screen.SearchScreen
import com.example.hilton.ui.screen.WelcomeScreen
import com.example.hilton.ui.theme.HiltonTheme
import com.example.hilton.vm.PokemonUi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    private val gson by lazy {
        GsonBuilder()
            .create()
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val controller = rememberNavController()
            HiltonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = controller,
                        startDestination = Route.WELCOME,
                        modifier = Modifier
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        composable(Route.WELCOME) {
                            WelcomeScreen {
                                controller.navigate(Route.SEARCH) {
                                    popUpTo(Route.WELCOME) {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                        composable(Route.SEARCH) {
                            SearchScreen {
                                val s = URLEncoder.encode(gson.toJson(it), Charset.defaultCharset())
                                controller.navigate(Route.DETAIL.plus(s))
                            }
                        }
                        composable(
                            Route.DETAIL.plus("{pokemon}"),
                            arguments = listOf(navArgument("pokemon") {
                                type = NavType.StringType
                            })
                        ) {
                            val result =
                                it.arguments?.getString("pokemon")
                            if (result != null) {
                                val pokemon = gson.fromJson(result, PokemonUi::class.java)
                                Detail(pokemonUi = pokemon) {
                                    controller.navigateUp()
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}
