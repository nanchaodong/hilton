package com.example.hilton.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Error(retry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = retry) {
            Text("Something error, click to Retry")
        }
    }
}

@Composable
fun Loading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun Empty() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("There is no any result")
    }
}
