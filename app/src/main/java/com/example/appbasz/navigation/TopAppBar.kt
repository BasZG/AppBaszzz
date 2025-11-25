package com.example.appbasz.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithNavigation(
    currentScreen: String,
    onHomeClick: () -> Unit,
    onLoginClick: () -> Unit,
    onCartClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Ruñau´s Store",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    icon = {
                        Text(
                            "Home",
                            fontWeight = if (currentScreen == "home") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = currentScreen == "home",
                    onClick = onHomeClick
                )
                NavigationBarItem(
                    icon = {
                        Text(
                            "Login",
                            fontWeight = if (currentScreen == "login") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = currentScreen == "login",
                    onClick = onLoginClick
                )
                NavigationBarItem(
                    icon = {
                        Text(
                            "Carrito",
                            fontWeight = if (currentScreen == "cart") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    selected = currentScreen == "cart",
                    onClick = onCartClick
                )
            }
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}